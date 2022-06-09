package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class ParameterType implements Serializable{

	public final String name;
    public final java.util.List<String> regularExpressions;
    public final Boolean preferForRegularExpressionMatch;
    public final Boolean useForSnippets;
    public final String id;
	public ParameterType(String name, List<String> regularExpressions, Boolean preferForRegularExpressionMatch,
			Boolean useForSnippets, String id) {
		this.name = name;
		this.regularExpressions = regularExpressions;
		this.preferForRegularExpressionMatch = preferForRegularExpressionMatch;
		this.useForSnippets = useForSnippets;
		this.id = id;
	}

}
