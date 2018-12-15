package cucumber.eclipse.steps.integration;

import java.util.Set;

public class StepDefinitionsChanged implements IStepDefinitionsRepositoryEvent {

	private Set<StepDefinition> stepDefinitions;

	public StepDefinitionsChanged(Set<StepDefinition> stepDefinitions) {
		super();
		this.stepDefinitions = stepDefinitions;
	}

	public Set<StepDefinition> getStepDefinitions() {
		return stepDefinitions;
	}
	
}
