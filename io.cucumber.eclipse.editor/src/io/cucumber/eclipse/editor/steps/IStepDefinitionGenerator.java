package io.cucumber.eclipse.editor.steps;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public interface IStepDefinitionGenerator {

	TextEdit createStepSnippet(String step, IDocument targetDocument) throws IOException, CoreException;

}
