package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestRunFinished  implements Serializable {

    public String message;
    public Boolean success;
    public Timestamp timestamp;
	public TestRunFinished(String message, Boolean success, Timestamp timestamp) {
		this.message = message;
		this.success = success;
		this.timestamp = timestamp;
	}

}
