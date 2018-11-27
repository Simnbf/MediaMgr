package com.sbf.MediaManager;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbf.model.TVDBApiLoginDetails;

/**
 * Unit test for simple App.
 */
public class AppTest {
	private static final Logger LOG = LoggerFactory.getLogger(AppTest.class);

	/**
	 * Creates JSON file from Object
	 */
	@Test
	public void createJSON() {
		// Create Login Details object with data from the txt file
		TVDBApiLoginDetails loginDetails = new TVDBApiLoginDetails();
		loginDetails.setApikey("RFZ6H3EVTU0RPXBO");
		loginDetails.setUserkey("101ZWS9B3ZKSIXHD");
		loginDetails.setUsername("simnbf7gu");

		// Create the new json file
		File file = new File("TVDBlogin.json");
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			// write object to file
			mapper.writeValue(file, loginDetails);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
