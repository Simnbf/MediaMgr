package com.sbf.MediaManager;


import java.io.File;

import java.io.IOException;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetParams {
	private static final Logger LOG = LoggerFactory.getLogger(GetParams.class);
	public static List<String> ReadParams(String parm) throws IOException{
		LOG.info("in GetParams class");	
		File file = new File(parm);
		List<String> parmOutput = FileUtils.readLines(file, "ASCII");
		return parmOutput;
	}

}
