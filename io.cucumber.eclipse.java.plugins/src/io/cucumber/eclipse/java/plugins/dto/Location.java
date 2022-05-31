package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class Location implements Serializable  {
	public Long line;
	public Long column;
	public Location(Long line, Long column) {
		this.line = line;
		this.column = column;
	}
}