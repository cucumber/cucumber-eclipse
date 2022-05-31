package io.cucumber.eclipse.java.plugins.dto;

import io.cucumber.messages.types.StepDefinitionPatternType;

public class StepDefinition {
	public String id;
    public StepDefinitionPattern pattern;
    public SourceReference sourceReference;
    
    public static class StepDefinitionPattern {
        public String source;
        public StepDefinitionPatternType type;
    }
}
