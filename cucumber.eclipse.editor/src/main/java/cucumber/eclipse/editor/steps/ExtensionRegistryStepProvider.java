package cucumber.eclipse.editor.steps;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getIntegrationExtensionsOfType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepsChangedEvent;

public class ExtensionRegistryStepProvider implements IStepProvider, IStepListener {

	private Set<Step> steps = new HashSet<Step>();

	private List<IStepDefinitions> stepDefinitions = getIntegrationExtensionsOfType(IStepDefinitions.class);

	private IFile file;
	
	public ExtensionRegistryStepProvider(IFile file) {
		this.file = file;
		reloadSteps();
		addStepListener(this);
	}

	public void addStepListener(IStepListener listener) {
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.addStepListener(listener);
		}
	}

	public Set<Step> getStepsInEncompassingProject() {
		return steps;
	}

	private void reloadSteps() {
		steps.clear();
		for (IStepDefinitions stepDef : stepDefinitions) {
			steps.addAll(stepDef.getSteps(file));
		}
	}

	public void removeStepListener(IStepListener listener) {
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.removeStepListener(listener);
		}
	}

	@Override
	public void onStepsChanged(StepsChangedEvent event) {
		reloadSteps();
	}
}
