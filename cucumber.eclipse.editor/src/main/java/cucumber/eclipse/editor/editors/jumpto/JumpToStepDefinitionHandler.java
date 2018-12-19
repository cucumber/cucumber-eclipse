package cucumber.eclipse.editor.editors.jumpto;

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
import cucumber.eclipse.steps.integration.ResourceUtil;
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
				String stepDefinitionPath = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_PATH_ATTRIBUTE);
				String stepDefinitionText = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE);
				Integer stepDefinitionLineNumber = (Integer) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_LINE_NUMBER_ATTRIBUTE);
				
				StepDefinition stepDefinition = new StepDefinition();
				stepDefinition.setSource(ResourceUtil.find(stepDefinitionPath));
				stepDefinition.setText(stepDefinitionText);
				stepDefinition.setLineNumber(stepDefinitionLineNumber);
				
				JumpToStepDefinition.openEditor(stepDefinition);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	

}
