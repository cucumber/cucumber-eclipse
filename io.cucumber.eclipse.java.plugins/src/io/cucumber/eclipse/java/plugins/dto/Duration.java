package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class Duration implements Serializable{
    public Long seconds;
    public Long nanos;
	public Duration(Long seconds, Long nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}
}