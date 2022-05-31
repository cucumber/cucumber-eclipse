package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

import io.cucumber.messages.types.AttachmentContentEncoding;
import io.cucumber.messages.types.Source;

public class Attachment implements Serializable{
	public String body;
	public AttachmentContentEncoding contentEncoding;
	public String fileName;
	public String mediaType;
	public Source source;
	public String testCaseStartedId;
	public String testStepId;
	public String url;
	public Attachment(String body, AttachmentContentEncoding contentEncoding, String fileName, String mediaType,
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
