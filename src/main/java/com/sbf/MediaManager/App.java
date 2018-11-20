package com.sbf.MediaManager;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;

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
					String target = mountPoint + "/" + parameters.get(mediaType);
					copyFilesToTarget(landingArea, filesFound.get(x).toString(), target);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int findMediaType(File workingFile) {
		String titlename = workingFile.getName();
		// TV shows almost always contain an S0xE0x qualifier, check for that and
		// substring up to it
		if (titlename.contains("S0")) {
			titlename = titlename.substring(0, titlename.indexOf("S0")).replace(".", " ").trim().replace(" ", "%20");
			// Invoke TVDB API to see if it is a TV show
			try {
				if (TVDBApi.searchShows(titlename)) {
					titlename = titlename.replace("%20", " ");
					LOG.info(titlename + " Is a TV show");
					return 4;
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
		return 99;	// never executed but Java wants it
	}

	public static void copyFilesToTarget(String srcbase, String file2Copy, String trgDir) throws IOException {

		File inFile = new File(file2Copy);
		// Get either the file name or directory, depending on what this is
		String lowQual = inFile.getName();

		String z = file2Copy.replace(srcbase, trgDir);

		File target = new File(z);
		if (inFile.isDirectory()) {
			target.mkdirs();
		}

		// If this "file" is actually a directory, we'll create it in target so that the
		// files are distributed into directories
		if (inFile.isDirectory()) {
			if (target.mkdir()) {
				LOG.info("Directory " + lowQual + " created in target " + z);
				return;
			} else {
				LOG.info("Directory " + lowQual + " already exists in target " + z);
				return;
			}
		}

		// Just a regular file, let's do the copy
		LOG.info("Copying " + file2Copy + " to " + z);
		
//TODO when we're moving a file from a sub-directory, it leaves the dir behind. Could do with identifying this and deleting it after the move
		

		try {
			FileUtils.moveFile(inFile, target);
			return;			
		} catch (FileExistsException e) {
			LOG.info("File in landing area already exists in target - should delete in future");
			e.printStackTrace();

		}
		return;
	}
}
