package cucumber.eclipse.editor.steps;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getStepDefinitionsProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import cucumber.eclipse.steps.integration.IStepDefinitionsProvider;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

/**
 * The uniq point of truth for step definitions independently of the language
 * used to define the step definitions.
 * 
 * @author qvdk
 *
 */
public class UniversalStepDefinitionsProvider implements IStepDefinitionsProvider {

	public static final UniversalStepDefinitionsProvider INSTANCE = new UniversalStepDefinitionsProvider();

	private StepDefinitionsStorage stepDefinitionsStorage = StepDefinitionsStorage.INSTANCE;

	private transient List<IStepDefinitionsProvider> stepDefinitionsProviders = getStepDefinitionsProvider();

	private transient List<IProject> initializedProjects = new ArrayList<IProject>();
	
	private UniversalStepDefinitionsProvider() {
	}

	public Set<StepDefinition> getStepDefinitions(IProject project) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		return stepDefinitionsRepository.getAllStepDefinitions();
	}

	public Set<IFile> getStepDefinitionsFiles(IProject project) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		return stepDefinitionsRepository.getAllStepDefinitionsFiles();
	}

	@Override
	public Set<StepDefinition> findStepDefinitions(IFile resource, MarkerFactory markerFactory,
			IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		boolean isFile = resource instanceof IFile;
		if (!isFile) {
			return new HashSet<StepDefinition>();
		}

		IFile stepDefinitionFile = (IFile) resource;
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Scan steps definitions for " + resource.getName(),
				stepDefinitionsProviders.size());

		IProject project = resource.getProject();
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);

		int stepDefinitionsCounter = 0;
		for (IStepDefinitionsProvider stepDefinitionsService : stepDefinitionsProviders) {
			if (stepDefinitionsService.support(project)) {
				Set<StepDefinition> stepDefs = stepDefinitionsService.findStepDefinitions(stepDefinitionFile,
						markerFactory, subMonitor);
				if (!stepDefs.isEmpty()) {
					stepDefinitionsCounter += stepDefs.size();
					// System.out.println(stepDefinitionsService.supportedLanguage() + " found " +
					// stepDefs.size() + " step definitions in " + stepDefinitionFile.getName());
				}
				stepDefinitionsRepository.add(stepDefinitionFile, stepDefs);
			}
		}
		long end = System.currentTimeMillis();
		long duration = end - start;
		if (stepDefinitionsCounter > 0 || duration > 0) {
			System.out.println("findStepDefs (" + resource.getName() + ") return " + stepDefinitionsCounter
					+ " step definitions in " + (duration) + "ms.");
		}
		return stepDefinitionsRepository.getAllStepDefinitions();
	}

	public void clean(IProject project) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project);
		stepDefinitionsRepository.reset();
	}

	public void persist(IProject project, IProgressMonitor monitor) throws IOException, CoreException {
		this.stepDefinitionsStorage.persist(project, monitor);
	}

	public void load(IProject project) throws CoreException {
		this.stepDefinitionsStorage.load(project, null);
		this.initializedProjects.add(project);
	}

	@Override
	public boolean support(IProject project) throws CoreException {
		for (IStepDefinitionsProvider stepDefinitionsProvider : stepDefinitionsProviders) {
			if(stepDefinitionsProvider.support(project)) {
				return true;
			} // else try the next
		}
		return false;
	}

	@Override
	public boolean support(IResource resource) throws CoreException {
		if(resource instanceof IFile) {
			for (IStepDefinitionsProvider stepDefinitionsProvider : stepDefinitionsProviders) {
				if(stepDefinitionsProvider.support(resource)) {
					return true;
				} // else try the next
			}
		}
		return false;
	}

	public boolean isInitialized(IProject project) {
		return initializedProjects.contains(project);
	}

}
