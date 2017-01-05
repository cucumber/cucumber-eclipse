package cucumber.eclipse.editor.steps;

import java.util.Set;

import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;

public interface IStepProvider {

	void addStepListener(IStepListener listener);
	
	Set<Step> getStepsInEncompassingProject();

	void removeStepListener(IStepListener listener);
}
