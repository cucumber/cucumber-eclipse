package cucumber.eclipse.editor.snippet;

import org.eclipse.core.resources.IFile;

import io.cucumber.eclipse.editor.document.IStepDefinitionGenerator;

public interface IStepGeneratorProvider {

	IStepDefinitionGenerator getStepGenerator(IFile targetFile);
}
