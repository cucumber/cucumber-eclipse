package cucumber.eclipse.editor.snippet;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.IStepGenerator;

public interface IStepGeneratorProvider {

	IStepGenerator getStepGenerator(IFile targetFile);
}
