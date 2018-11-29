package com.sbf.MediaManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TVDBApi {
	public static final Logger LOG = LoggerFactory.getLogger(TVDBApi.class);

	public static boolean searchShows(String searchTitle) throws IOException {

		URL url = null;
		try {
			url = new URL("https://api.thetvdb.com/search/series?name=" + searchTitle);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return false;
		}
		String jwt = "";

		try {
			jwt = getJWTToken();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		HttpsURLConnection con;
//		LOG.info(url.toString());
		con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		String bearer = "Bearer " + jwt;
		con.setRequestProperty("Authorization", bearer);
		con.setRequestProperty("content-type", "application/json");
		int httpStatus = con.getResponseCode();

		if (httpStatus == 200) {
			if (App.verbose) {
				LOG.info(searchTitle + " Found on TVDB");
			}
			con.disconnect();
			return true;
			
		}
		if (httpStatus == 404) {
			searchTitle = URLDecoder.decode(searchTitle, "UTF-8");
			LOG.info(searchTitle + " Not found on TVDB");
			con.disconnect();
			return false;
		}
		// if (httpStatus < 200 && httpStatus > 300) {
		LOG.info("Bad HTTP Status on TVDB GET API: " + httpStatus + " " + con.getResponseMessage());
		con.disconnect();
		return false;

	}

	public static String getJWTToken() throws IOException {
		String jwt = "wrong";
		URL url = null;
		try {
			url = new URL("https://api.thetvdb.com/login");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return jwt;
		}
		
		HttpsURLConnection con;
//		LOG.info(url.toString());
		con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-type", "application/json");
		con.setDoOutput(true);
		con.setDoInput(true);
		
		// Created JSON file
		File json = new File("TVDBlogin.json");
		
		try (InputStream is = new FileInputStream(json)) {
			try(OutputStream ostream = con.getOutputStream()) {
				// Straight pipe from file to output stream
				inToOut(is, ostream);
				// always flush after use
				ostream.flush();
			}
		}
		
		int httpStatus = con.getResponseCode();

		if (httpStatus < 200 && httpStatus > 300) {
			LOG.info("Bad HTTP Status on login POST: " + httpStatus);
			return jwt;
		}

		StringBuilder sb = new StringBuilder();

		/* This is called a try with resources, when using resources that need to be closed instead of
		 * having to have a try catch around this and then a finally in order to ensure its closed
		 * you can just do try(ObjectThatNeedsToClose instance = getInstance)
		 * and it will automatically close it even if it throws an exception during processing
		 */
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))){
			String postPayload;
			while ((postPayload = br.readLine()) != null) {
				sb.append(postPayload + "\n");
			}
		}

		
		
		JSONObject tokenData = new JSONObject(sb.toString());
		jwt = tokenData.getString("token");
		con.disconnect();
		return jwt;
	}
	
	/**
	 * Super simple get byte from {@code is} and write it to {@code os} holds nothing in memory
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	private static void inToOut(final InputStream is, final OutputStream os) throws IOException {
		byte[] bytes;
		while (is.available() > 0) {
			bytes = new byte[1];
			is.read(bytes);
			os.write(bytes);
		}
		
	}

}