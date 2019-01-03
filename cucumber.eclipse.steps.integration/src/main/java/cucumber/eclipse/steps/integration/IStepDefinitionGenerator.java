package cucumber.eclipse.steps.integration;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public interface IStepDefinitionGenerator {

	boolean supports(IFile stepFile);
	
	TextEdit createStepSnippet(gherkin.formatter.model.Step step,
	        IDocument targetDocument) throws IOException, CoreException;
	
}
