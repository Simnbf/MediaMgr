package com.sbf.MediaManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMDBApi {
	public static final Logger LOG = LoggerFactory.getLogger(TMDBApi.class);

	public static JSONObject searchMovies(String searchTitle) throws IOException {

		URL url = null;
		String movieTitle = "not found";
		JSONObject outData = null;
		
		
		File apikey = new File("TMDBlogin.json");
		org.json.simple.JSONObject loginInfo = new org.json.simple.JSONObject();
		loginInfo = GetParams.ReadParams(apikey);
		String authkey = (String) loginInfo.get("apikey");
		// No JWT token for MVDB, just need the API Key		
		try {
			url = new URL("https://api.themoviedb.org/3/search/movie?api_key=" + authkey + "&query=" + searchTitle);
		} catch (MalformedURLException e1) {
			LOG.error(e1.toString()); 
			return outData;
		}

		HttpsURLConnection con;
//		LOG.info(url.toString());
		con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("content-type", "application/json");
		int httpStatus = con.getResponseCode();
		if (httpStatus == 200) {
			if (App.verbose) {
				LOG.info(searchTitle + " Found on MVDB");
			}

			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				String postPayload;
				while ((postPayload = br.readLine()) != null) {
					sb.append(postPayload + "\n");
				}		
				outData = new JSONObject(sb.toString());						
			}
		}

		if (httpStatus < 200 && httpStatus > 300) {
			LOG.error("Bad HTTP Status on MVDB GET API: " + httpStatus + " " + con.getResponseMessage());
		}
		con.disconnect();
		return outData;
	}
}