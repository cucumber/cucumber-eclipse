package cucumber.eclipse.editor.steps;

import static io.cucumber.eclipse.editor.ExtensionRegistryUtil.getStepDefinitionsProvider;

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

import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinitionsRepository;
import io.cucumber.eclipse.editor.steps.StepDefinitionsStorage;

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
		return getStepDefinitions(project, new HashSet<>());
	}

	private Set<StepDefinition> getStepDefinitions(IProject project, Set<String> analyzedProjects) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project, null);
		Set<StepDefinition> stepDefinitions = stepDefinitionsRepository.getAllStepDefinitions(); // step definitions of the current projects
		
		IProject[] referencedProjects = project.getReferencedProjects();
		for (IProject referencedProject : referencedProjects) {
			if (!analyzedProjects.contains(referencedProject.getName())) {
				// prevent the project from being analyzed twice
				analyzedProjects.add(referencedProject.getName());

				Set<StepDefinition> stepDefinitionsFromReferencedProject = getStepDefinitions(referencedProject, analyzedProjects);
				stepDefinitions.addAll(stepDefinitionsFromReferencedProject);
			}
		}
		
		return stepDefinitions;
	}

	public Set<IFile> getStepDefinitionsFiles(IProject project) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project, null);
		return stepDefinitionsRepository.getAllStepDefinitionsFiles();
	}

	@Override
	public Set<StepDefinition> findStepDefinitions(IResource stepDefinitionResource, MarkerFactory markerFactory,
			IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Scan steps definitions for " + stepDefinitionResource.getName(),
				stepDefinitionsProviders.size());

		IProject project = stepDefinitionResource.getProject();
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project, null);

		int stepDefinitionsCounter = 0;
		for (IStepDefinitionsProvider stepDefinitionsService : stepDefinitionsProviders) {
			if (stepDefinitionsService.support(project)) {
				Set<StepDefinition> stepDefs = stepDefinitionsService.findStepDefinitions(stepDefinitionResource,
						markerFactory, subMonitor);
				if (!stepDefs.isEmpty()) {
					stepDefinitionsCounter += stepDefs.size();
					// System.out.println(stepDefinitionsService.supportedLanguage() + " found " +
					// stepDefs.size() + " step definitions in " + stepDefinitionFile.getName());
				}
				stepDefinitionsRepository.add(stepDefinitionResource, stepDefs);
			}
		}
		long end = System.currentTimeMillis();
		long duration = end - start;
		if (stepDefinitionsCounter > 0 || duration > 0) {
			System.out.println("findStepDefs (" + stepDefinitionResource.getName() + ") return " + stepDefinitionsCounter
					+ " step definitions in " + (duration) + "ms.");
		}
		return stepDefinitionsRepository.getAllStepDefinitions();
	}

	public void clean(IProject project) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.stepDefinitionsStorage.getOrCreate(project, null);
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
		//if(resource instanceof IFile) {
			for (IStepDefinitionsProvider stepDefinitionsProvider : stepDefinitionsProviders) {
				if(stepDefinitionsProvider.support(resource)) {
					return true;
				} // else try the next
			}
//		}
		return false;
	}

	public boolean isInitialized(IProject project) {
		return initializedProjects.contains(project);
	}

}
