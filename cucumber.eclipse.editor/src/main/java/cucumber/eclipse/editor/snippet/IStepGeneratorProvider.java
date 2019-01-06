package cucumber.eclipse.editor.snippet;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.IStepDefinitionGenerator;

public interface IStepGeneratorProvider {

	IStepDefinitionGenerator getStepGenerator(IFile targetFile);
}
