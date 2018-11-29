package com.sbf.MediaManager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	public static final Logger LOG = LoggerFactory.getLogger(App.class);
	public static boolean verbose = false;
	public static void main(String[] args) {
				
		try {
			if (args[0] == "-v") {
				verbose = true;
			}
		} catch (ArrayIndexOutOfBoundsException e){
			verbose = false;
		}
		
		String parmFile = "parms.txt";
		List<String> parameters = new ArrayList<String>();
		LOG.info("--------------------");
		LOG.info("Starting up.");

		// Never do this. It's fine in C but it's not a Java standard
		String nasIP = "", landingArea = "", mountPoint = "";
		try {
			parameters = GetParams.ReadParams(parmFile);
			nasIP = parameters.get(1);
			mountPoint = parameters.get(2);
			landingArea = mountPoint + "/" + parameters.get(3);		
			if (verbose) {
				LOG.info("nasIP = " + nasIP);
				LOG.info("landingArea = " + landingArea);
			}
			InetAddress nas = InetAddress.getByName(nasIP);
			if (nas.isReachable(1000)) {
				LOG.info("Successfully ping'd NAS");
			} else {
				LOG.info("NAS IP " + nasIP + " unreachable, ending.");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<File> filesFound = new ArrayList<File>();
		try {
			filesFound = ListFiles.listAllFiles(landingArea);
			if (filesFound.size() == 1) {
				LOG.info("No files in " + landingArea + " - ending.");
				LOG.info("--------------------");
				return;
			}
			for (int x = 0; x < filesFound.size(); x++) {
				if (filesFound.get(x).isFile()) {
					int mediaType = findMediaType(filesFound.get(x));
					if (mediaType > 0) {
						String target = mountPoint + "/" + parameters.get(mediaType);
						// Can just use Files.copy(from, to);
						try {
							copyFilesToTarget(landingArea, filesFound.get(x).toString(), target);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.info("--------------------");
	}

	public static int findMediaType(File workingFile) {
		String titlename = workingFile.getName();
		// Throw it out if its not a media file
		int mediaType = 0;
		int x = titlename.lastIndexOf(".");
		String y = titlename.substring(x);
		switch (y) {
		case ".txt":
		case ".dat":
		case ".info":
		case ".parts":
			LOG.info(titlename + " Is not a media file");
			return mediaType;
		default:
			break;
		}
		if (titlename.substring(0, 1) == ".") {
			LOG.info(titlename + " is not a media file");
			return mediaType;
		}
		if (verbose) {
			LOG.info(titlename + " appears to be a media file");
		}
		// TV shows almost always contain an S0xE0x qualifier, check for that and
		// substring up to it
		titlename = titlename.toUpperCase();
		if (titlename.contains("S0")) {
			titlename = titlename.substring(0, titlename.indexOf("S0")).replace(".", " ").trim();
			try {
				titlename = URLEncoder.encode(titlename, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				LOG.error(e.getMessage(), e);
			}
			// Invoke TVDB API to see if it is a TV show
			try {
				if (TVDBApi.searchShows(titlename)) {
					titlename = titlename.replace("%20", " ");
					LOG.info(titlename.replace("+", " ") + " Is a TV show");
					mediaType = 4;
				} else {
					mediaType = 5;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			/*
			 * Could be a movie with the (CCYY) date in it, replace that with spaces I got
			 * real fucking lazy here and decided that if I couldn't spot S0* in the file
			 * name it's a movie. In the future I'd like this to call the movie DB also
			 */
			LOG.info("Defaulting " + titlename + " to a movie");
			mediaType = 5;
		}
		return mediaType;
	}

	public static void copyFilesToTarget(String baseFrom, String file2Copy, String baseTarget)
			throws IOException, InterruptedException {

		File inFile = new File(file2Copy);
		// Get the file name, going to see if there's a dir setup for this media in
		// target
		String mediaName = inFile.getName();

		try {
			mediaName = mediaName.substring(0, mediaName.indexOf("S0")).replace(".", " ").trim();
		} catch (StringIndexOutOfBoundsException e) {
			mediaName = mediaName.substring(0, mediaName.lastIndexOf(".")); // Strip off the extension and use this as
																			// dir name
		}
		String targetDir = baseTarget + "/" + mediaName;

		File target = new File(file2Copy.replace(baseFrom, targetDir));

		
/*		try {
			FileUtils.moveFile(inFile, target);
		} catch (FileExistsException e) {
			LOG.info("File in landing area already exists in target - should delete in future");
			LOG.error("FileExistsException");
		}*/

		LOG.info("Copied " + mediaName + " to " + target.toString());

		return;
	}
}
