package cucumber.eclipse.steps.integration;

import org.eclipse.core.resources.IFile;

public class StepsChangedEvent {

	private IFile stepDefinitionFile = null;

	public StepsChangedEvent() {
	}

	/**
	 * Create a steps changed event from a given step definition file
	 * 
	 * @param stepDefinitionFile the step definitions file where the update occurs
	 */
	public StepsChangedEvent(IFile stepDefinitionFile) {
		super();
		this.stepDefinitionFile = stepDefinitionFile;
	}

	public IFile getStepDefinitionFile() {
		return stepDefinitionFile;
	}

}
