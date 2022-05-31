package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

import io.cucumber.messages.types.StepDefinitionPatternType;

public class StepDefinition implements Serializable{
	public String id;
    public StepDefinitionPattern pattern;
    public SourceReference sourceReference;
    
    public static class StepDefinitionPattern implements Serializable{
        public String source;
        public StepDefinitionPatternType type;
		public StepDefinitionPattern(String source, StepDefinitionPatternType type) {
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
