package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;


public class Attachment implements Serializable{
	public String body;
	public String contentEncoding;
	public String fileName;
	public String mediaType;
	public Source source;
	public String testCaseStartedId;
	public String testStepId;
	public String url;
	public Attachment(String body, String contentEncoding, String fileName, String mediaType,
			Source source, String testCaseStartedId, String testStepId, String url) {
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
