package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;


public class TestStepFinished implements Serializable {

	public final String testCaseStartedId;
    public final String testStepId;
    public final TestStepResult testStepResult;
    public final Timestamp timestamp;
    
    public static class TestStepResult implements Serializable {
        public final Duration duration;
        public final String message;
        public final String status;
		public TestStepResult(Duration duration, String message, String status) {
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
