package cucumber.eclipse.editor.steps;

import java.util.Set;

import cucumber.eclipse.steps.integration.Step;

public interface IStepProvider {

	Set<Step> getStepsInEncompassingProject();

}
