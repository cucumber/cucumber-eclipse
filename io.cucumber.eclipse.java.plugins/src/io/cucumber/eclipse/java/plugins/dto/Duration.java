package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class Duration implements Serializable{
    public final Long seconds;
    public final Long nanos;
	public Duration(Long seconds, Long nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}
}