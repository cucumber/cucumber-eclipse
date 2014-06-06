package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IFile;

public interface IStepDefinitions {

	Set<Step> getSteps(IFile featurefile);
}
