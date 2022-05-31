package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class TestRunStarted implements Serializable {

	public Timestamp timestamp;

	public TestRunStarted(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

}
