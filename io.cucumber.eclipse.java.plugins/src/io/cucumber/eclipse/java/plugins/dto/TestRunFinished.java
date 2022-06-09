package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestRunFinished  implements Serializable {

    public final String message;
    public final Boolean success;
    public final Timestamp timestamp;
	public TestRunFinished(String message, Boolean success, Timestamp timestamp) {
		this.message = message;
		this.success = success;
		this.timestamp = timestamp;
	}

}
