package com.sbf.MediaManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.*;

import javax.net.ssl.HttpsURLConnection;

public class TVDBApi {
	public static final Logger LOG = LoggerFactory.getLogger(App.class);

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
		LOG.info(url.toString());
		con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		String bearer = "Bearer " + jwt;
		con.setRequestProperty("Authorization", bearer);
		con.setRequestProperty("content-type", "application/json");
		int httpStatus = con.getResponseCode();

		if (httpStatus == 200) {
			con.disconnect();
			return true;
		}

		// if (httpStatus < 200 && httpStatus > 300) {
		LOG.info("Bad HTTP Status on login POST: " + httpStatus);
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
		LOG.info(url.toString());
		con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-type", "application/json");
		con.setDoOutput(true);
		con.setDoInput(true);

		JSONObject obj = new JSONObject();
		obj.put("apikey", "RFZ6H3EVTU0RPXBO");
		obj.put("userkey", "101ZWS9B3ZKSIXHD");
		obj.put("username", "simnbf7gu");
		String jwtPOST = obj.toString();

		OutputStream ostream = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(ostream, "UTF-8");
		osw.write(jwtPOST);
		osw.flush();
		osw.close();
		ostream.close();

		int httpStatus = con.getResponseCode();

		if (httpStatus < 200 && httpStatus > 300) {
			LOG.info("Bad HTTP Status on login POST: " + httpStatus);
			return jwt;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String postPayload;
		while ((postPayload = br.readLine()) != null) {
			sb.append(postPayload + "\n");
		}
		br.close();

		JSONObject tokenData = new JSONObject(sb.toString());
		jwt = tokenData.getString("token");
		con.disconnect();
		return jwt;
	}

}