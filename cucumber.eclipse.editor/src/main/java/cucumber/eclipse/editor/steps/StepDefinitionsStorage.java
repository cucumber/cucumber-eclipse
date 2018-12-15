package cucumber.eclipse.editor.steps;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class StepDefinitionsStorage implements Storage<StepDefinitionsRepository> {

	public static final StepDefinitionsStorage INSTANCE = new StepDefinitionsStorage();
	
	private Map<IProject, StepDefinitionsRepository> stepDefinitionsByProject = new HashMap<IProject, StepDefinitionsRepository>();

	private StepDefinitionsStorage() {
	}
	
	@Override
	public StepDefinitionsRepository getOrCreate(IProject project) {
		StepDefinitionsRepository stepProvider = stepDefinitionsByProject.get(project);
		if(stepProvider == null) {
			stepProvider = new StepDefinitionsRepository();
			this.add(project, stepProvider);
		}
		return stepProvider;
	}

	@Override
	public void add(IProject project, StepDefinitionsRepository stepDefinitionsRepository) {
		this.stepDefinitionsByProject.put(project, stepDefinitionsRepository);
	}

}
