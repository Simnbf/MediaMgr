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

	public static void main(String[] args) {
		String parmFile = args[0];
		List<String> parameters = new ArrayList<String>();
		LOG.info("Starting up.");
		
		// Never do this. It's fine in C but it's not a Java standard
		String nasIP = "", landingArea = "", mountPoint = "";
		try {
			parameters = GetParams.ReadParams(parmFile);
			nasIP = parameters.get(1);
			mountPoint = parameters.get(2);
			landingArea = mountPoint + "/" + parameters.get(3);

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
			if (filesFound.isEmpty()) {
				LOG.info("No files in " + landingArea + " - ending.");
				return;
			}
			for (int x = 0; x < filesFound.size(); x++) {
				if (filesFound.get(x).isFile()) {
					int mediaType = findMediaType(filesFound.get(x));
					if (mediaType > 0) {
						String target = mountPoint + "/" + parameters.get(mediaType);
						// Can just use Files.copy(from, to);
						copyFilesToTarget(landingArea, filesFound.get(x).toString(), target);
					}
				}
				
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int findMediaType(File workingFile) {
		String titlename = workingFile.getName();
		// Throw it out if its not a media file
		int x=titlename.lastIndexOf(".");
		String y = titlename.substring(x);
		switch (y) {
			case ".txt": case ".dat": case ".info": 
				LOG.info(titlename + " Is not a media file");
				return 0;
			default:
				break;
		}
		// TV shows almost always contain an S0xE0x qualifier, check for that and
		// substring up to it		
		if (titlename.contains("S0")) {
			titlename = titlename.substring(0, titlename.indexOf("S0")).replace(".", " ").trim().replace(" ", "%20");
			try {
				titlename = URLEncoder.encode(titlename, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				LOG.error(e.getMessage(), e);
			}
			// Invoke TVDB API to see if it is a TV show
			try {
				if (TVDBApi.searchShows(titlename)) {
					titlename = titlename.replace("%20", " ");
					LOG.info(titlename + " Is a TV show");
					return 4;					
				} else {
					return 5;
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
			LOG.info(titlename + " Is a movie");
			return 5;
		}
		/*
		 * You should only really have one exit point for a method
		 * if you set a int result object and where you return you should set that
		 * and then return it here.
		 */
		return 99;	// never executed but Java wants it
	}

	public static void copyFilesToTarget(String baseFrom, String file2Copy, String baseTarget) throws IOException {

		File inFile = new File(file2Copy);
		// Tell JVM to delete everything in from directory when we end
//		inFile.deleteOnExit();
		// Get the file name, going to see if there's a dir setup for this media in target
		String mediaName = inFile.getName();
		try {
			mediaName = mediaName.substring(0, mediaName.indexOf("S0")).replace(".", " ").trim(); //.replace(" ", "%20");
		} catch (StringIndexOutOfBoundsException e) {
			mediaName = mediaName.substring(0, mediaName.lastIndexOf("."));
		}		
		String targetDir = baseTarget + "/" + mediaName;
				
		File target = new File(file2Copy.replace(baseFrom, targetDir));

		LOG.info("Copying " + mediaName + " to " + target.toString());		
				
/*		try {
			FileUtils.moveFile(inFile, target);
			return;			
		} catch (FileExistsException e) {
			LOG.info("File in landing area already exists in target - should delete in future");
			LOG.error("FileExistsException");

		}*/
		return;
	}
}
