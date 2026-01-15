package io.cucumber.eclipse.python.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;
import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.eclipse.python.launching.BehaveProcessLauncher;
import io.cucumber.eclipse.python.validation.BehaveGlueStore;
import io.cucumber.eclipse.python.validation.StepMatch;
import io.cucumber.messages.types.Step;

/**
 * Opens Python step definitions when user Ctrl+Clicks on a step
 */
@Component(service = IStepDefinitionOpener.class)
public class PythonStepDefinitionOpener implements IStepDefinitionOpener {

	@Override
	public boolean canOpen(IResource resource) throws CoreException {
		return BehaveProcessLauncher.isBehaveProject(resource);
	}

	@Override
	public boolean openInEditor(ITextViewer textViewer, IResource resource, Step step) throws CoreException {
		if (resource == null || step == null) {
			return false;
		}

		IDocument document = textViewer.getDocument();
		BehaveGlueStore glueStore = Activator.getBehaveGlueStore();
		Collection<StepMatch> matchedSteps = glueStore != null 
				? glueStore.getMatchedSteps(document) 
				: Collections.emptyList();

		// Find the step match for this step based on line number
		int stepLine = step.getLocation().getLine().intValue();
		StepMatch match = matchedSteps.stream().filter(m -> m.getFeatureLine() == stepLine).findFirst().orElse(null);

		if (match == null) {
			return false;
		}

		try {
			// Open the Python file at the specified line
			IProject project = resource.getProject();
			String stepFile = match.getStepFile();

			// Resolve the step file path relative to the project
			File file = new File(project.getLocation().toFile(), stepFile);
			if (!file.exists()) {
				// Try as absolute path
				file = new File(stepFile);
			}

			if (file.exists()) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				// Use the default editor for .py files instead of hardcoding PyDev editor
				IEditorPart editorPart = IDE.openEditor(page,
						project.getWorkspace().getRoot()
								.getFileForLocation(new Path(file.getAbsolutePath())));

				// Navigate to the line
				if (editorPart instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) editorPart;
					IDocument targetDoc = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
					if (targetDoc != null) {
						// Line numbers are 1-based in the step match, but 0-based in the document
						int lineOffset = targetDoc.getLineOffset(match.getStepLine() - 1);
						textEditor.selectAndReveal(lineOffset, 0);
					}
				}
				return true;
			}
		} catch (PartInitException | org.eclipse.jface.text.BadLocationException e) {
			EditorLogging.error("Failed to open Python step definition", e);
		}

		return false;
	}
}
