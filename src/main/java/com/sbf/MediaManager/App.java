package com.sbf.MediaManager;

import java.io.File;
import org.json.simple.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	public static final Logger LOG = LoggerFactory.getLogger(App.class);
	public static boolean verbose = false;
	public static String mediaType  = "unknown"; 
	public static String Title = "not found";	// This will be the folder name in the target directory

	public static void main(String[] args) throws InterruptedException {

		try {
			if (args[0] == "[-v]") {
				verbose = true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			verbose = false;
		}

		File parmFile = new File("control.json");
		LOG.info("--------------------");
		LOG.info("Starting up.");
		String nasIP = "";
		String landingArea = "";
		String mountPoint = "";
		JSONObject parmData = null;
		
		try {
			parmData = GetParams.ReadParams(parmFile);
			nasIP = parmData.get("nasIP").toString();
			mountPoint = parmData.get("nasMountPoint").toString();
			landingArea = mountPoint + "/" + parmData.get("landingArea").toString();

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
			LOG.info(e.toString());
		}

		List<File> filesFound = new ArrayList<File>();
		try {
			filesFound = ListFiles.listAllFiles(landingArea);
			if (filesFound.size() == 1) {
				LOG.info("No files in " + landingArea + " - ending.");
				LOG.info("--------------------");
				return;
			}
			// See if we're allowed to carry on
			boolean carryon = continueWork(filesFound);
			if (!carryon) {
				LOG.info("Execution control found, ending.");
				LOG.info("--------------------");
				return;
			}
			
			for (int x = 0; x < filesFound.size(); x++) {
				if (filesFound.get(x).isFile()) {
					mediaType = identifyMediaType(filesFound.get(x));
					String target = mountPoint + "/" + parmData.get(mediaType).toString();
					copyFilesToTarget(landingArea, filesFound.get(x).toString(), target);
				}
			}
		} catch (IOException e) {
			LOG.error(e.toString());
		}
		/*	Now that we've copied everything we'll go and delete and directories left behind  */
		try {
			filesFound = ListFiles.listAllFiles(landingArea);
			for (int x=1; x< filesFound.size(); x++) {
				if(filesFound.get(x).isDirectory()) {
					deleteLeftoverDirectories(filesFound.get(x));
				}
			}
		} catch (IOException e) {
			LOG.error(e.toString());
		}
		LOG.info("--------------------");
	}

	public static String identifyMediaType(File workingFile) {

		// Check it's an actual media file and not some other crap.
		boolean isMedia = isMediaFile(workingFile);
		if (!isMedia) {
			return "unknown";
		}
		boolean suspectedTv = false;
		String workingTitle = workingFile.getName();
		// TV shows almost always contain an S0xE0x qualifier, check for that and
		// substring up to it
		workingTitle = workingTitle.toUpperCase();
		int maxIndex = 3;
		String[] season = new String[maxIndex ];
		season[0] = "S0";
		season[1] = "S1";
		season[2] = "S2";
		int seasonidx = 0;
		for (int index = 0; index < maxIndex ; index++) {
			if (workingTitle.contains(season[index])) {
				suspectedTv = true;
				seasonidx = index;
			}
		}			
		if (suspectedTv) {
			// Looks like this has a season identifier, we'll trim the title more + search
			// the TVDB
			workingTitle = workingTitle.substring(0, workingTitle.indexOf(season[seasonidx])).replace(".", " ").trim();
			try {
				String searchQuery = URLEncoder.encode(workingTitle, "UTF-8");
				Title = TVDBApi.searchShows(searchQuery);
				if (Title == "not found") {
					LOG.info("Got a not found from TVDB, will check MVDB now");
					suspectedTv = false;
				} else {
					LOG.info(workingFile.getName() + " Appears to be an episode of " + Title);
					mediaType = "tvDir";
				}
			} catch (UnsupportedEncodingException e) {
				LOG.error(e.getMessage(), e);
			}
			  catch (IOException e) {
				e.printStackTrace();
			}			
		} 
		if (!suspectedTv) {
			/* Decided it's likely not a TV episode, we'll call the movie DB to see if it's a film
			 * Might need to figure out how to strip the file name down. Let's see.
			 */
			try {
				// Remove file extension + replace any .'s with nothings
				workingTitle = workingTitle.substring(0, workingTitle.lastIndexOf(".")).replace(".",  " ").trim();
				// replace any fun characters, ([])
				workingTitle = workingTitle.replaceAll("\\[(.*?)\\]", " ").replaceAll("\\((.*?)\\)", " ").trim();
				int a = workingTitle.length();
				if (a > 20) {
					workingTitle = workingTitle.substring(0,20);
				}
				String searchQuery = URLEncoder.encode(workingTitle, "UTF-8");
				org.json.JSONObject outData = new org.json.JSONObject();
				outData = TMDBApi.searchMovies(searchQuery);
				int resultSize = outData.getInt("total_results");
				if (resultSize > 0) {
					JSONArray js = null;
					js = (JSONArray) outData.get("results");					
					outData = js.getJSONObject(0);
					Title = outData.getString("title");
				} else {
					LOG.error("Nothing found on MVDB, but going to default to a movie");
					Title = workingFile.getName();
				}
				mediaType = "mvDir";			
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return mediaType;
	}
	public static void copyFilesToTarget(String baseFrom, String file2Copy, String baseTarget)
			throws IOException, InterruptedException {

		File inFile = new File(file2Copy);
		String mediaTitle = inFile.getName();
		File target = new File(baseTarget + "/" + Title + "/" + inFile.getName());
		try {
			FileUtils.moveFile(inFile, target);
		} catch (FileExistsException e) {
			LOG.info("File in landing area already exists in target - should delete in future");
			LOG.error(e.toString());
		}
		String logText = "Copied " + mediaTitle + " to " + target.toString();
		LOG.info(logText);
		return;
	}
	private static void deleteLeftoverDirectories(File toDelete) {
		try {
			FileUtils.deleteDirectory(toDelete);
			LOG.info("Successfully deleted " + toDelete.toString());
		} catch (IOException e) {
			LOG.error("Error deleting directory: " + toDelete.toString());
			LOG.error(e.toString());
		}
	}
	public static boolean continueWork(List filesFound) {
		boolean OktoContinue = false;
		for (int index = 0; index < filesFound.size(); index++) {
			String a = filesFound.get(index).toString();
			if (a.contains("_mm")) {
				if (a.contains("on")) {
					OktoContinue = true;
				}
				filesFound.remove(index);
			}
		}
		return OktoContinue;
	}
	public static boolean isMediaFile(File workingFile) {
		
		String fileName = workingFile.getName();
		// Throw it out if its not a media file
		int x = fileName.lastIndexOf(".");
		String y = fileName.substring(x).toLowerCase();
		switch (y) {
		case ".txt":
		case ".dat":
		case ".info":
		case ".parts":
		case ".jpeg":
		case ".jpg":
		case ".gif":
		case ".html":
		case ".ds_store":
			LOG.info(fileName + " Is not a media file, deleting");
			try {
				FileUtils.forceDelete(workingFile);
			} catch (IOException e1) {
				LOG.info(e1.toString());			
			}
			return false;
		default:
			break;
		}
		return true;
	}
}
	
