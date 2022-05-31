package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestCaseStarted   implements Serializable{
    public Long attempt;
    public String id;
    public String testCaseId;
    public Timestamp timestamp;
	public TestCaseStarted(Long attempt, String id, String testCaseId, Timestamp timestamp) {
		this.attempt = attempt;
		this.id = id;
		this.testCaseId = testCaseId;
		this.timestamp = timestamp;
	}
}
