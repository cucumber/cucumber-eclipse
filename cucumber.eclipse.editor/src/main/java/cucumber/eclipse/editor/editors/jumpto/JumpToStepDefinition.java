package cucumber.eclipse.editor.editors.jumpto;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.ExtensionRegistryUtil;
import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinitionsRepository;
import io.cucumber.eclipse.editor.steps.StepDefinitionsStorage;

class JumpToStepDefinition {

	public static StepDefinition findStepDefinitionMatch(int selectionLineNumber, IFile gherkinFile)
			throws CoreException {
		IMarker stepDefinitionMatchMarker = JumpToStepDefinition.findStepDefinitionMatchMarker(selectionLineNumber,
				gherkinFile);
		if (stepDefinitionMatchMarker != null) {
			String id = (String) stepDefinitionMatchMarker
					.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_JDT_HANDLE_IDENTIFIER_ATTRIBUTE);
			//Search step in repository
			if (id != null) {
				StepDefinitionsRepository repository = StepDefinitionsStorage.INSTANCE
						.getOrCreate(gherkinFile.getProject(), null);
				Set<StepDefinition> stepDefinitions = repository.getAllStepDefinitions();
				for (StepDefinition step : stepDefinitions) {
					if (id.equals(step.getId())) {
						return step;
					}
				}
			}
		}
		return null;
	}

	public static IMarker findStepDefinitionMatchMarker(int selectionLineNumber, IFile gherkinFile)
			throws CoreException {
		IMarker stepDefinitionMatchMarker = null;
		IMarker[] markers = gherkinFile.findMarkers(MarkerFactory.STEP_DEFINTION_MATCH, false, IResource.DEPTH_ZERO);
		for (IMarker marker : markers) {
			Integer lineNumber = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			if (lineNumber.equals(selectionLineNumber)) {
				stepDefinitionMatchMarker = marker;
				break;
			}
		}

		if (stepDefinitionMatchMarker == null) {
			return null;
		}
		return stepDefinitionMatchMarker;
	}

	public static void openEditor(StepDefinition stepDefinition) {
		try {
			List<IStepDefinitionOpener> openers = ExtensionRegistryUtil.getStepDefinitionOpener();
			for (IStepDefinitionOpener opener : openers) {
				if (opener.canOpen(stepDefinition)) {
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
