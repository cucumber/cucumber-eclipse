package cucumber.eclipse.editor.steps;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getStepDefinitionsProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import cucumber.eclipse.steps.integration.IStepDefinitionsProvider;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public class ExtensionRegistryStepProvider implements IStepDefinitionsProvider {

	public static final ExtensionRegistryStepProvider INSTANCE = new ExtensionRegistryStepProvider();
	
	private StepDefinitionsStorage stepDefinitionsStorage = StepDefinitionsStorage.INSTANCE;
	
	private List<IStepDefinitionsProvider> stepDefinitionsProviders = getStepDefinitionsProvider();
	
	
	private ExtensionRegistryStepProvider() {
	}
	
	public Set<StepDefinition> getStepDefinitions(IProject project) {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		return stepDefinitionsRepository.getAllStepDefinitions();
	}
	
	public Set<IFile> getStepDefinitionsFiles(IProject project) {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		return stepDefinitionsRepository.getAllStepDefinitionsFiles();
	}

	@Override
	public Set<StepDefinition> findStepDefinitions(IFile resource, MarkerFactory markerFactory, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		boolean isFile = resource instanceof IFile;
		if(!isFile) {
			return new HashSet<StepDefinition>();
		}
		
		IFile stepDefinitionFile = (IFile) resource;
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Scan steps definitions for " + resource.getName(), stepDefinitionsProviders.size());

		IProject project = resource.getProject();
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		
		int stepDefinitionsCounter = 0;
		for (IStepDefinitionsProvider stepDefinitionsService : stepDefinitionsProviders) {
			if(stepDefinitionsService.support(project)) {
				Set<StepDefinition> stepDefs = stepDefinitionsService.findStepDefinitions(stepDefinitionFile, markerFactory, subMonitor);
				if(!stepDefs.isEmpty()) {
					stepDefinitionsCounter += stepDefs.size();
		//			System.out.println(stepDefinitionsService.supportedLanguage() + " found " + stepDefs.size() + " step definitions in " + stepDefinitionFile.getName());
				}
				stepDefinitionsRepository.add(stepDefinitionFile, stepDefs);
			}
		}
		long end = System.currentTimeMillis();
		long duration = end - start;
		if(stepDefinitionsCounter > 0 || duration > 0) {
			System.out.println("findStepDefs (" + resource.getName() + ") return " + stepDefinitionsCounter + " step definitions in " + (duration) + "ms.");
		}
		return stepDefinitionsRepository.getAllStepDefinitions();
	}

	public void clean(IProject project) {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		stepDefinitionsRepository.reset();
	}

	@Override
	public boolean support(IProject project) throws CoreException {
		boolean isSupported = false;
		for (IStepDefinitionsProvider stepDefinitionsProvider : stepDefinitionsProviders) {
			isSupported = isSupported || stepDefinitionsProvider.support(project);
		}
		return isSupported;
	}
}
