package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class TestCase  implements Serializable{

    public String id;
    public String pickleId;
    public java.util.List<TestStep> testSteps;
    
    public static class TestStep  implements Serializable {
        public String hookId;
        public String id;
        public String pickleStepId;
        public java.util.List<String> stepDefinitionIds;
        public java.util.List<StepMatchArgumentsList> stepMatchArgumentsLists;
		public TestStep(String hookId, String id, String pickleStepId, List<String> stepDefinitionIds,
				List<StepMatchArgumentsList> stepMatchArgumentsLists) {
			this.hookId = hookId;
			this.id = id;
			this.pickleStepId = pickleStepId;
			this.stepDefinitionIds = stepDefinitionIds;
			this.stepMatchArgumentsLists = stepMatchArgumentsLists;
		}
    }
    public static class StepMatchArgumentsList  implements Serializable {
        public java.util.List<StepMatchArgument> stepMatchArguments;

		public StepMatchArgumentsList(List<StepMatchArgument> stepMatchArguments) {
			this.stepMatchArguments = stepMatchArguments;
		}
    }
    
    public static class StepMatchArgument  implements Serializable{
       public Group group;
       public String parameterTypeName;
	public StepMatchArgument(Group group, String parameterTypeName) {
		this.group = group;
		this.parameterTypeName = parameterTypeName;
	}
    }
    
    public static class Group  implements Serializable{
        public java.util.List<Group> children;
        public Long start;
        public String value;
		public Group(List<Group> children, Long start, String value) {
			this.children = children;
			this.start = start;
			this.value = value;
		}
    }

	public TestCase(String id, String pickleId, List<TestStep> testSteps) {
		this.id = id;
		this.pickleId = pickleId;
		this.testSteps = testSteps;
	}
}
