package cucumber.eclipse.steps.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StepDefinitionsStorage {

	private Map<String, Set<Step>> storage = new HashMap<String, Set<Step>>();
	
	public void add(Set<Step> steps) {
		for (Step step : steps) {
			this.add(step);
		}
	}
	
	public void add(Step step) {
		String ressourceName = step.getSource().getName();
		Set<Step> stepsForThisRessource = storage.get(ressourceName);
		if(stepsForThisRessource == null) {
			stepsForThisRessource = new HashSet<Step>();
		}
		stepsForThisRessource.add(step);
		this.storage.put(ressourceName, stepsForThisRessource);
	}
	
	public Set<Step> getAllSteps() {
		Set<Step> allSteps = new HashSet<Step>();
		for (Set<Step> steps : storage.values()) {
			allSteps.addAll(steps);
		}
		return allSteps;
	}
}
