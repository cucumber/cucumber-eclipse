package cucumber.eclipse.editor.steps;

import java.util.Set;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.Step;

public interface IStepProvider {

	Set<Step> getStepsInEncompassingProject(IFile featurefile);

}
