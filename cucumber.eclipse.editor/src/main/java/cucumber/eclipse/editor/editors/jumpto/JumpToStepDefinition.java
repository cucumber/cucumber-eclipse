package cucumber.eclipse.editor.editors.jumpto;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.util.ExtensionRegistryUtil;
import cucumber.eclipse.steps.integration.IStepDefinitionOpener;
import cucumber.eclipse.steps.integration.ResourceHelper;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

class JumpToStepDefinition {
	
	public static StepDefinition findStepDefinitionMatch(int selectionLineNumber, IFile gherkinFile) throws CoreException {
		StepDefinition stepDefinition = null;
		IMarker stepDefinitionMatchMarker = JumpToStepDefinition.findStepDefinitionMatchMarker(selectionLineNumber, gherkinFile);
		if(stepDefinitionMatchMarker != null) {
			String stepDefinitionPath = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_PATH_ATTRIBUTE);
			if(stepDefinitionPath == null) {
				// unable to jump
				return null;
			}
			String stepDefinitionText = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE);
			Integer stepDefinitionLineNumber = (Integer) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_LINE_NUMBER_ATTRIBUTE);
			
			stepDefinition = new StepDefinition();
			stepDefinition.setSource(new ResourceHelper().find(stepDefinitionPath));
			stepDefinition.setText(stepDefinitionText);
			stepDefinition.setLineNumber(stepDefinitionLineNumber);
		}
		return stepDefinition;
	}
	
	public static IMarker findStepDefinitionMatchMarker(int selectionLineNumber, IFile gherkinFile) throws CoreException {
		IMarker stepDefinitionMatchMarker = null; 
		IMarker[] markers = gherkinFile.findMarkers(MarkerFactory.STEP_DEFINTION_MATCH, false, IResource.DEPTH_ZERO);
		for (IMarker marker : markers) {
			Integer lineNumber = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			if(lineNumber.equals(selectionLineNumber)) {
				stepDefinitionMatchMarker = marker;
				break;
			}
		}

		if(stepDefinitionMatchMarker == null) {
			return null;
		}
		return stepDefinitionMatchMarker;
	}

	public static void openEditor(StepDefinition stepDefinition) {
		try {
			List<IStepDefinitionOpener> openers = ExtensionRegistryUtil.getStepDefinitionOpener();
			for (IStepDefinitionOpener opener : openers) {
				if(opener.canOpen(stepDefinition)) {
					opener.openInEditor(stepDefinition);
					return;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Failed to open step definition %s", stepDefinition.getSource().getFullPath()), e));
		}
	}

}
