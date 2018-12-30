package com.sbf.MediaManager;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;

import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;


public class GetParams {
	private static final Logger LOG = LoggerFactory.getLogger(GetParams.class);
	public static JSONObject ReadParams(File parmFile) throws IOException{
		LOG.info("Reading " + parmFile.toString() + " file");
		FileReader reader = new FileReader(parmFile);
		
		JSONParser parse = new JSONParser();
		JSONObject jsbr = null;
		try {
			jsbr = (JSONObject) parse.parse(reader);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return jsbr;
	}
}
