package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;


public class Source implements Serializable {
	public String uri;
	public String data;
	public String mediaType;
	public Source(String uri, String data, String mediaType) {
		this.uri = uri;
		this.data = data;
		this.mediaType = mediaType;
	}
}
