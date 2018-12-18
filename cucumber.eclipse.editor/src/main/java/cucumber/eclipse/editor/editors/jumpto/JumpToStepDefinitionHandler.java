package cucumber.eclipse.editor.editors.jumpto;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import cucumber.eclipse.editor.editors.Editor;
import cucumber.eclipse.steps.integration.SerializationHelper;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public class JumpToStepDefinitionHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);

		// Needs to be a gherkin editor for this to work, if not then simply do nothing.
		if (!(editorPart instanceof Editor)) {
			return null;
		}
		
		Editor editor = (Editor) editorPart;

		TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();
		
		int selectionLineNumber = selection.getStartLine() + 1;
		
		IFile gherkinFile = editor.getFile();

		// Search a step definition match marker on this file at the selected line
		try {
			IMarker stepDefinitionMatchMarker = JumpToStepDefinition.findStepDefinitionMatchMarker(selectionLineNumber, gherkinFile);
			if(stepDefinitionMatchMarker != null) {
				String serializedStepDefinition = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_STEPDEF_ATTRIBUTE);
				StepDefinition stepDefinition = SerializationHelper.deserialize(serializedStepDefinition);
				
				JumpToStepDefinition.openEditor(stepDefinition);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	

}
