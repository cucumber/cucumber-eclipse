package cucumber.eclipse.editor.editors.jumpto;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.steps.integration.ResourceUtil;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

class JumpToStepDefinition {
	
	public static StepDefinition findStepDefinitionMatch(int selectionLineNumber, IFile gherkinFile) throws CoreException {
		StepDefinition stepDefinition = null;
		IMarker stepDefinitionMatchMarker = JumpToStepDefinition.findStepDefinitionMatchMarker(selectionLineNumber, gherkinFile);
		if(stepDefinitionMatchMarker != null) {
			String stepDefinitionPath = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_PATH_ATTRIBUTE);
			String stepDefinitionText = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE);
			Integer stepDefinitionLineNumber = (Integer) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_LINE_NUMBER_ATTRIBUTE);
			
			stepDefinition = new StepDefinition();
			stepDefinition.setSource(ResourceUtil.find(stepDefinitionPath));
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
			IResource file = stepDefinition.getSource();
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(IMarker.LINE_NUMBER, stepDefinition.getLineNumber());
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Failed to open step definition %s", stepDefinition.getSource().getFullPath()), e));
		}

	}


}
