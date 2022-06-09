package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public  class Timestamp implements Serializable {
    public final Long seconds;
    public final Long nanos;
	public Timestamp(Long seconds, Long nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}
}