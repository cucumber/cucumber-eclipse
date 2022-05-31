package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class ParameterType implements Serializable{

	public String name;
    public java.util.List<String> regularExpressions;
    public Boolean preferForRegularExpressionMatch;
    public Boolean useForSnippets;
    public String id;
	public ParameterType(String name, List<String> regularExpressions, Boolean preferForRegularExpressionMatch,
			Boolean useForSnippets, String id) {
		this.name = name;
		this.regularExpressions = regularExpressions;
		this.preferForRegularExpressionMatch = preferForRegularExpressionMatch;
		this.useForSnippets = useForSnippets;
		this.id = id;
	}

}
