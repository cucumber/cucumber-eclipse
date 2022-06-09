package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class ParseError implements Serializable {
	public final SourceReference source;
    public final String message;
	public ParseError(SourceReference source, String message) {
		this.source = source;
		this.message = message;
	}
}
