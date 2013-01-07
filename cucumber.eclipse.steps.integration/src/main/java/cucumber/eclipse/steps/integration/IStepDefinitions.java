package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IProject;

public interface IStepDefinitions {

	Set<Step> getSteps(IProject project);
}
