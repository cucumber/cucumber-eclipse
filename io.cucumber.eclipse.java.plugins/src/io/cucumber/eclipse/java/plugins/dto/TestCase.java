package io.cucumber.eclipse.java.plugins.dto;


public class TestCase {

    public String id;
    public String pickleId;
    public java.util.List<TestStep> testSteps;
    
    public static class TestStep {
        public String hookId;
        public String id;
        public String pickleStepId;
        public java.util.List<String> stepDefinitionIds;
        public java.util.List<StepMatchArgumentsList> stepMatchArgumentsLists;
    }
    public static class StepMatchArgumentsList {
        public java.util.List<StepMatchArgument> stepMatchArguments;
    }
    
    public static class StepMatchArgument {
       public Group group;
       public String parameterTypeName;
    }
    
    public static class Group {
        public java.util.List<Group> children;
        public Long start;
        public String value;
    }
}
