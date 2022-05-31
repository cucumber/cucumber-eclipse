package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public  class Timestamp implements Serializable {
    public Long seconds;
    public Long nanos;
	public Timestamp(Long seconds, Long nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}
}