package com.sbf.MediaManager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TVDBApiTest {
	private static final Logger LOG = LoggerFactory.getLogger(TVDBApiTest.class);
	
	@Test
	public void testGetJWTTocken() {
		try {
			String jwt = TVDBApi.getJWTToken();
			LOG.info("JWT Result :" + jwt);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
