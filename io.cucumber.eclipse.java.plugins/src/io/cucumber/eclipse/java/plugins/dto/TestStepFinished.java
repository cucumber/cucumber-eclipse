package io.cucumber.eclipse.java.plugins.dto;

import io.cucumber.messages.types.TestStepResultStatus;

public class TestStepFinished {

	public String testCaseStartedId;
    public String testStepId;
    public TestStepResult testStepResult;
    public Timestamp timestamp;
    
    public static class TestStepResult {
        public Duration duration;
        public String message;
        public TestStepResultStatus status;
    }
}
