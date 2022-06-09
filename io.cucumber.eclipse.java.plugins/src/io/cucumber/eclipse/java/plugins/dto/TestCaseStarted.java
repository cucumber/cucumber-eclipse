package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestCaseStarted   implements Serializable{
    public final Long attempt;
    public final String id;
    public final String testCaseId;
    public final Timestamp timestamp;
	public TestCaseStarted(Long attempt, String id, String testCaseId, Timestamp timestamp) {
		this.attempt = attempt;
		this.id = id;
		this.testCaseId = testCaseId;
		this.timestamp = timestamp;
	}
}
