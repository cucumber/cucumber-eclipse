package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;


public class Attachment implements Serializable{
	public final String body;
	public final String contentEncoding;
	public final String fileName;
	public final String mediaType;
	public final SourceReference source;
	public final String testCaseStartedId;
	public final String testStepId;
	public final String url;
	public Attachment(String body, String contentEncoding, String fileName, String mediaType,
			SourceReference source, String testCaseStartedId, String testStepId, String url) {
		this.body = body;
		this.contentEncoding = contentEncoding;
		this.fileName = fileName;
		this.mediaType = mediaType;
		this.source = source;
		this.testCaseStartedId = testCaseStartedId;
		this.testStepId = testStepId;
		this.url = url;
	}


}
