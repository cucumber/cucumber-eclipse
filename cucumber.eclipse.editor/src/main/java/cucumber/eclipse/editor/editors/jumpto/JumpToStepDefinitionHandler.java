package cucumber.eclipse.editor.editors.jumpto;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.editor.editors.Editor;
import cucumber.eclipse.editor.steps.GlueRepository;
import cucumber.eclipse.editor.steps.GlueRepository.Glue;

public class JumpToStepDefinitionHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);

		// Needs to be a gherkin editor for this to work, if not then simply do nothing.
		if (!(editorPart instanceof Editor)) {
			return null;
		}
		
		ITextEditor editor = (ITextEditor) editorPart;
		TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();


		IDocument document = editor.getDocumentProvider().getDocument(editorPart.getEditorInput());
		
		int offset = selection.getOffset();
		
		int lineStartOffset = 0;
		
		IRegion lineInfo = null;
		String currentLine = null;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			lineStartOffset = lineInfo.getOffset();
			currentLine = document.get(lineStartOffset, lineInfo.getLength());
		} catch (BadLocationException e) {
			return null;
		}
		
		// find the gherkin step
		// get the related step definition
		// get the related step definitions file
		// open this last one
		
		Glue glue = GlueRepository.INSTANCE.findGlue(currentLine.trim());
		if(glue == null) {
			// no glue found
			return null;
		}
		
		JumpToStepDefinition.openEditor(glue.getStepDefinition());
		
		return null;
	}

}
