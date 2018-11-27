package com.sbf.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "loginDetails")
public class TVDBApiLoginDetails {
	private String apikey;
	private String userkey;
	private String username;

	public TVDBApiLoginDetails() {
		super();
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getUserkey() {
		return userkey;
	}

	public void setUserkey(String userkey) {
		this.userkey = userkey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
