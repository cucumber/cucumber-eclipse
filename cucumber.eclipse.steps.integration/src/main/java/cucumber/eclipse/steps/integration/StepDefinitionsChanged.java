package cucumber.eclipse.steps.integration;

import java.util.Set;

public class StepDefinitionsChanged implements IStepDefinitionsRepositoryEvent {

	private Set<Step> stepDefinitions;

	public StepDefinitionsChanged(Set<Step> stepDefinitions) {
		super();
		this.stepDefinitions = stepDefinitions;
	}

	public Set<Step> getStepDefinitions() {
		return stepDefinitions;
	}
	
}
