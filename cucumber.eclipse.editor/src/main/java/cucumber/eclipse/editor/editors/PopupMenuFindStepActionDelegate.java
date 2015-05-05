package cucumber.eclipse.editor.editors;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.steps.integration.Step;

public class PopupMenuFindStepActionDelegate extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);
		IEditorInput input = editorPart.getEditorInput();

		// Editor contents needs to be associated with an eclipse project
		// for this to work, if not then simply do nothing.
		if (!(input instanceof IFileEditorInput)) {
			return null;
		}
		IFile featurefile = ((IFileEditorInput) input).getFile();

		Set<Step> steps = FeatureFileUtil.getStepsInEncompassingProject(featurefile);
		
		String selectedLine = getSelectedLine(editorPart);
		String language = FeatureFileUtil.getDocumentLanguage(editorPart);

		Step matchedStep = new StepMatcher().matchSteps(language, steps, selectedLine);
		try {
			if (matchedStep != null) openEditor(matchedStep);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void openEditor(Step step) throws CoreException {
		   IResource file = step.getSource();
		   IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		   HashMap<String, Integer> map = new HashMap<String, Integer>();
		   map.put(IMarker.LINE_NUMBER, step.getLineNumber());
		   IMarker marker = file.createMarker(IMarker.TEXT);
		   marker.setAttributes(map);
		   IDE.openEditor(page, marker);
		   marker.delete();
		   
	}
	
	private String getSelectedLine(IEditorPart editorPart) {
		
		ITextEditor editor = (ITextEditor) editorPart;
				
		TextSelection selecton = (TextSelection) editor.getSelectionProvider().getSelection();
		int line = selecton.getStartLine();	
		
		IDocumentProvider docProvider = editor.getDocumentProvider();
		IDocument doc = docProvider.getDocument(editorPart.getEditorInput());
		try {
			String stepLine = doc.get(doc.getLineOffset(line), doc.getLineLength(line)).trim();
			return stepLine;
		} catch (BadLocationException e) {
			return "";
		}
	}
}
