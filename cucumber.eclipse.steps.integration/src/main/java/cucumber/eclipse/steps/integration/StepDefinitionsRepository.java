package cucumber.eclipse.steps.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Storage class for step definitions.
 * 
 * Step definitions are store by resource name in order to be able to update
 * them partially.
 * 
 * Thus, when we computes step definitions from only one resource, the previous
 * computed step definitions for this resource are overwritten. And we don't 
 * have to compute step definitions for all projects to have a real view of 
 * step definitions. 
 * 
 * 
 * @author qvdk
 *
 */
public class StepDefinitionsRepository {

	public static final StepDefinitionsRepository INSTANCE = new StepDefinitionsRepository();

	private Map<IFile, Set<Step>> stepsByResourceName;
	private Set<StepDefinitionsRepositoryListener> listeners;

	private StepDefinitionsRepository() {
		this.reset();
		this.listeners = new HashSet<StepDefinitionsRepositoryListener>();
	}

	public void add(IFile stepDefinitionsFile, List<Step> steps) {
		this.stepsByResourceName.put(stepDefinitionsFile, new HashSet<Step>(steps));
	}

	public void add(IFile stepDefinitionsFile, Set<Step> steps) {
		this.stepsByResourceName.put(stepDefinitionsFile, steps);
	}

	public Set<IFile> getAllStepDefinitionsFiles() {
		return this.stepsByResourceName.keySet();
	}
	
	public Set<Step> getAllStepDefinitions() {
		Set<Step> allSteps = new HashSet<Step>();
		for (Set<Step> steps : stepsByResourceName.values()) {
			allSteps.addAll(steps);
		}
		return allSteps;
	}
	
	public boolean isStepDefinitions(IFile file) {
		return this.stepsByResourceName.containsKey(file);
	}

	public void reset() {
		this.stepsByResourceName = new HashMap<IFile, Set<Step>>();
	}

	public void addListener(StepDefinitionsRepositoryListener listener) {
		this.listeners.add(listener);
	}

	protected void fireStepDefinitionsChangedEvent(Set<Step> stepDefinitions) {
		StepDefinitionsChanged event = new StepDefinitionsChanged(stepDefinitions);
		for (StepDefinitionsRepositoryListener listener : listeners) {
			listener.onStepDefinitionsChanged(event);
		}
	}

	protected void fireStepDefinitionsReset() {
		StepDefinitionsResetEvent event = new StepDefinitionsResetEvent();
		for (StepDefinitionsRepositoryListener listener : listeners) {
			listener.onReset(event);
		}
	}
}
