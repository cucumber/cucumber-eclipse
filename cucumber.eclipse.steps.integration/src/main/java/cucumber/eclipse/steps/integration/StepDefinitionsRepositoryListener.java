package cucumber.eclipse.steps.integration;

public interface StepDefinitionsRepositoryListener {

	void onStepDefinitionsChanged(StepDefinitionsChanged event);
	
	void onReset(StepDefinitionsResetEvent reset); 
	
}
