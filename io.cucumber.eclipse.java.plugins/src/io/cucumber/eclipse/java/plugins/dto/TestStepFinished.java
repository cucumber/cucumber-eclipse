package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

import io.cucumber.messages.types.TestStepResultStatus;

public class TestStepFinished implements Serializable {

	public String testCaseStartedId;
    public String testStepId;
    public TestStepResult testStepResult;
    public Timestamp timestamp;
    
    public static class TestStepResult implements Serializable {
        public Duration duration;
        public String message;
        public TestStepResultStatus status;
		public TestStepResult(Duration duration, String message, TestStepResultStatus status) {
			this.duration = duration;
			this.message = message;
			this.status = status;
		}
    }

	public TestStepFinished(String testCaseStartedId, String testStepId, TestStepResult testStepResult,
			Timestamp timestamp) {
		this.testCaseStartedId = testCaseStartedId;
		this.testStepId = testStepId;
		this.testStepResult = testStepResult;
		this.timestamp = timestamp;
	}
}
