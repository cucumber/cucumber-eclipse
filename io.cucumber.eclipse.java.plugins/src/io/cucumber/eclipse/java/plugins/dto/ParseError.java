package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class ParseError implements Serializable {
	public SourceReference source;
    public String message;
	public ParseError(SourceReference source, String message) {
		this.source = source;
		this.message = message;
	}
}
