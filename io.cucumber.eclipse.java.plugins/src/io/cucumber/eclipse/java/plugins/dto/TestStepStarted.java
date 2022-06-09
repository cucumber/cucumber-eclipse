package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestStepStarted implements Serializable{

	public final String testCaseStartedId;
	public final String testStepId;
	public final Timestamp timestamp;
	public TestStepStarted(String testCaseStartedId, String testStepId, Timestamp timestamp) {
		this.testCaseStartedId = testCaseStartedId;
		this.testStepId = testStepId;
		this.timestamp = timestamp;
	}

}
