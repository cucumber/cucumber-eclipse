package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

import io.cucumber.messages.types.SourceMediaType;

public class Source implements Serializable {
	public String uri;
	public String data;
	public SourceMediaType mediaType;
	public Source(String uri, String data, SourceMediaType mediaType) {
		this.uri = uri;
		this.data = data;
		this.mediaType = mediaType;
	}
}
