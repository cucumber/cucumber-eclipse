package cucumber.eclipse.steps.integration;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.TextEdit;

public interface IStepGenerator {

	boolean supports(IFile stepFile);
	
	TextEdit createStepSnippet(IFile stepFile, gherkin.formatter.model.Step step) throws IOException, CoreException;
}
