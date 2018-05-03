package cucumber.eclipse.editor.editors;

import static cucumber.eclipse.editor.editors.DocumentUtil.getDocumentLanguage;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.editor.markers.MarkerIds;
import cucumber.eclipse.editor.markers.MarkerManager;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepsChangedEvent;
import cucumber.eclipse.steps.jdt.StepDefinitions;
import gherkin.lexer.LexingError;
import gherkin.parser.Parser;

public class PopupMenuFindStepHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);

		// Needs to be a gherkin editor for this to work, if not then simply do nothing.
		if (!(editorPart instanceof Editor)) {
			return null;
		}
		
        StepDefinitions defs = StepDefinitions.getInstance();
        if (defs != null) {
            defs.notifyListeners(new StepsChangedEvent());
        }
		
		Editor editor = (Editor) editorPart;
		Set<Step> steps = editor.getStepProvider().getStepsInEncompassingProject();
		
		IDocumentProvider docProvider = editor.getDocumentProvider();
		List<String> selectedLineResolvedSteps = resolveSelectedLineStep(editorPart);
		String language = getDocumentLanguage(docProvider.getDocument(editorPart.getEditorInput()));
		for (String variant : selectedLineResolvedSteps) {
			Step matchedStep = new StepMatcher().matchSteps(language, steps, variant);
			try {
				if (matchedStep != null) {
					openEditor(matchedStep);
					break;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
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

	// go through all examples of current scenario outline and generate step strings with replaced variables values
	private List<String> resolveSelectedLineStep(IEditorPart editorPart) {
		ITextEditor editor = (ITextEditor) editorPart;
		TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();

		int currentLineNumber = selection.getStartLine() + 1;

		IDocument document = editor.getDocumentProvider().getDocument(editorPart.getEditorInput());
		
		IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
		IFile featureFile = fileEditorInput.getFile();
		MarkerManager markerManager = new MarkerManager();
		PopupMenuFindStepFormatter findStepFormatter = new PopupMenuFindStepFormatter(currentLineNumber);
		Parser p = new Parser(findStepFormatter, false);
		try {
			p.parse(document.get(), "", 0);
		} catch (LexingError l) {
			markerManager.add(MarkerIds.LEXING_ERROR, featureFile, IMarker.SEVERITY_ERROR, l.getLocalizedMessage(), 1, 0, 0);
		}
		return findStepFormatter.getResolvedStepNames();
	}
}
