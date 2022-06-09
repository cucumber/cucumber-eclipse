package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class Hook implements Serializable{
	public final String id;
    public final String name;
    public final SourceReference sourceReference;
    public final String tagExpression;
	public Hook(String id, String name, SourceReference sourceReference, String tagExpression) {
		this.id = id;
		this.name = name;
		this.sourceReference = sourceReference;
		this.tagExpression = tagExpression;
	}
}
