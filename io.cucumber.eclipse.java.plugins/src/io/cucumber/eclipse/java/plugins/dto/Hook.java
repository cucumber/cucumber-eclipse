package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class Hook implements Serializable{
	public String id;
    public String name;
    public SourceReference sourceReference;
    public String tagExpression;
	public Hook(String id, String name, SourceReference sourceReference, String tagExpression) {
		this.id = id;
		this.name = name;
		this.sourceReference = sourceReference;
		this.tagExpression = tagExpression;
	}
}
