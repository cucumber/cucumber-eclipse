package cucumber.eclipse.editor.steps;

import cucumber.eclipse.steps.integration.StepDefinitionsChanged;
import cucumber.eclipse.steps.integration.StepDefinitionsResetEvent;

public interface StepDefinitionsRepositoryListener {

	void onStepDefinitionsChanged(StepDefinitionsChanged event);
	
	void onReset(StepDefinitionsResetEvent reset); 
	
}
