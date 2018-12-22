package cucumber.eclipse.editor.steps;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import cucumber.eclipse.editor.Activator;

public class StepDefinitionsStorage implements BuildStorage<StepDefinitionsRepository> {

	public static final StepDefinitionsStorage INSTANCE = new StepDefinitionsStorage();

	private static final String BUILD_FILE = "cucumber.stepDefinitions.tmp";

	private Map<IProject, StepDefinitionsRepository> stepDefinitionsByProject = new HashMap<IProject, StepDefinitionsRepository>();

	private List<IProject> initializedProjects = new ArrayList<IProject>();
	
	private StepDefinitionsStorage() {
	}

	@Override
	public StepDefinitionsRepository getOrCreate(IProject project) throws CoreException {
		if(!initializedProjects.contains(project)) {
			this.load(project, null);
			this.initializedProjects.add(project);
		}
		StepDefinitionsRepository stepDefinitionRepository = stepDefinitionsByProject.get(project);
		if (stepDefinitionRepository == null) {
			stepDefinitionRepository = new StepDefinitionsRepository();
			this.add(project, stepDefinitionRepository);
			this.initializedProjects.add(project);
		}
		return stepDefinitionRepository;
	}

	@Override
	public void add(IProject project, StepDefinitionsRepository stepDefinitionsRepository) {
		this.stepDefinitionsByProject.put(project, stepDefinitionsRepository);
	}

	@Override
	public void persist(IProject project, IProgressMonitor monitor) throws CoreException {
		StepDefinitionsRepository stepDefinitionsRepository = this.getOrCreate(project);
		String stepDefinitionsRepositorySerialized;
		try {
			stepDefinitionsRepositorySerialized = StepDefinitionsRepository.serialize(stepDefinitionsRepository);
			StorageHelper.saveIntoBuildDirectory(BUILD_FILE, project, monitor, stepDefinitionsRepositorySerialized.getBytes());
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		
	}

	@Override
	public void load(IProject project, IProgressMonitor monitor) throws CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
        
		IFolder target = project.getFolder("target");
		if (!target.exists()) {
			return;
		}
		IFile buildFile = target.getFile(BUILD_FILE);
		if (!buildFile.exists()) {
			return;
		}
		InputStream inputStream = buildFile.getContents();
		String stepDefinitionsRepositorySerialized;
		try {
            subMonitor.setTaskName("Loading cucumber step definitions from a previous build");
			stepDefinitionsRepositorySerialized = StorageHelper.copy(inputStream);
			subMonitor.newChild(1);
			
			subMonitor.setTaskName("Deserialize cucumber step definitions");
			StepDefinitionsRepository stepDefinitionsRepository = StepDefinitionsRepository
					.deserialize(stepDefinitionsRepositorySerialized);
			this.add(project, stepDefinitionsRepository);
			subMonitor.newChild(1);
			subMonitor.done();
			System.out.println(stepDefinitionsRepository.getAllStepDefinitions().size() + " step definitions loaded for "+project.getName());
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		} catch (ClassNotFoundException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

}
