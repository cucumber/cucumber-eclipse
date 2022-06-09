package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestCaseFinished  implements Serializable {

	public final String testCaseStartedId;
    public final Timestamp timestamp;
    public final Boolean willBeRetried;
	public TestCaseFinished(String testCaseStartedId, Timestamp timestamp, Boolean willBeRetried) {
		this.testCaseStartedId = testCaseStartedId;
		this.timestamp = timestamp;
		this.willBeRetried = willBeRetried;
	}

}
