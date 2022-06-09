package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;


public class StepDefinition implements Serializable{
	public String id;
    public StepDefinitionPattern pattern;
    public SourceReference sourceReference;
    
    public static class StepDefinitionPattern implements Serializable{
        public String source;
        public String type;
		public StepDefinitionPattern(String source, String type) {
			this.source = source;
			this.type = type;
		}
    }

	public StepDefinition(String id, StepDefinitionPattern pattern, SourceReference sourceReference) {
		this.id = id;
		this.pattern = pattern;
		this.sourceReference = sourceReference;
	}
}
