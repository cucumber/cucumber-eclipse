package cucumber.eclipse.editor.steps;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import cucumber.eclipse.steps.integration.ResourceHelper;
import cucumber.eclipse.steps.integration.SerializationHelper;
import cucumber.eclipse.steps.integration.StepDefinition;

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

	private Map<IResource, Set<StepDefinition>> stepDefinitionsByResourceName;

	protected StepDefinitionsRepository() {
		this.stepDefinitionsByResourceName = new HashMap<IResource, Set<StepDefinition>>();
	}

	public void add(IResource stepDefinitionsFile, Set<StepDefinition> steps) {
		if(steps.isEmpty()) {
			this.stepDefinitionsByResourceName.remove(stepDefinitionsFile);
		}
		else {
			this.stepDefinitionsByResourceName.put(stepDefinitionsFile, steps);
		}
	}

	public Set<IFile> getAllStepDefinitionsFiles() {
		Set<IFile> fromFilesOnly = new HashSet<IFile>();
		for (IResource resource : this.stepDefinitionsByResourceName.keySet()) {
			if(resource instanceof IFile) {
				fromFilesOnly.add((IFile) resource);
			}
		}
		return fromFilesOnly;
	}
	
	public Set<StepDefinition> getAllStepDefinitions() {
		Set<StepDefinition> allSteps = new HashSet<StepDefinition>();
		for (Set<StepDefinition> steps : stepDefinitionsByResourceName.values()) {
			allSteps.addAll(steps);
		}
		return allSteps;
	}
	
	public boolean isStepDefinitionsResource(IResource resource) {
		return this.stepDefinitionsByResourceName.containsKey(resource);
	}

	public void reset() {
		this.stepDefinitionsByResourceName = new HashMap<IResource, Set<StepDefinition>>();
//		System.out.println("Reset step definitions");
	}
	

	public static String serialize(StepDefinitionsRepository stepDefinitionsRepository) throws IOException {
		// since IFile is not serializable, we need to transform them into string path
		Map<String, Set<StepDefinition>> stepDefinitionsByResourceName = new HashMap<String, Set<StepDefinition>>(stepDefinitionsRepository.stepDefinitionsByResourceName.size());
		
		Set<Entry<IResource,Set<StepDefinition>>> entrySet = stepDefinitionsRepository.stepDefinitionsByResourceName.entrySet();
		for (Entry<IResource, Set<StepDefinition>> entry : entrySet) {
			stepDefinitionsByResourceName.put(entry.getKey().getFullPath().toString(), entry.getValue());
		}
		return SerializationHelper.serialize(stepDefinitionsByResourceName);
	}
	
	public static StepDefinitionsRepository deserialize(String stepDefinitionsRepositorySerialized) throws ClassNotFoundException, IOException {
		return deserialize(stepDefinitionsRepositorySerialized, new ResourceHelper());
	}
	
	protected static StepDefinitionsRepository deserialize(String stepDefinitionsRepositorySerialized, ResourceHelper resourceHelper) throws ClassNotFoundException, IOException {
		Map<String, Set<StepDefinition>> stepDefinitionsByResourceName = SerializationHelper.deserialize(stepDefinitionsRepositorySerialized);
		
		Map<IResource,Set<StepDefinition>> stepDefinitionsByResource = new HashMap<IResource, Set<StepDefinition>>(stepDefinitionsByResourceName.size());

		Set<Entry<String,Set<StepDefinition>>> entrySet = stepDefinitionsByResourceName.entrySet();
		for (Entry<String, Set<StepDefinition>> entry : entrySet) {
			stepDefinitionsByResource.put((IResource) resourceHelper.find(entry.getKey()), entry.getValue());
		}
		
		StepDefinitionsRepository stepDefinitionsRepository = new StepDefinitionsRepository();
		stepDefinitionsRepository.stepDefinitionsByResourceName = stepDefinitionsByResource;
		return stepDefinitionsRepository;
	}
	
}
