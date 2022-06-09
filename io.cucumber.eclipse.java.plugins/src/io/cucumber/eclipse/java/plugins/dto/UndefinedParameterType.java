package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class UndefinedParameterType implements Serializable{

	public String expression;
	public String name;
	
	public UndefinedParameterType(String expression, String name) {
		this.expression = expression;
		this.name = name;
	}


}
