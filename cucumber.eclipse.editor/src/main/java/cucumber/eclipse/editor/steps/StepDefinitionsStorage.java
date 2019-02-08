package cucumber.eclipse.editor.steps;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
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
	public StepDefinitionsRepository getOrCreate(IProject project, IProgressMonitor monitor) throws CoreException {
		if (!initializedProjects.contains(project)) {
			this.load(project, monitor);
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
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Persiting step definitions", 100);
		StepDefinitionsRepository stepDefinitionsRepository = this.getOrCreate(project, subMonitor.newChild(10));
		try {
			try (InputStream inputStream = StorageHelper.toStream(stepDefinitionsRepository, subMonitor.newChild(80))) {
				StorageHelper.saveIntoBuildDirectory(BUILD_FILE, project, subMonitor.newChild(10), inputStream);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}

	}

	@Override
	public void load(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder outputFolder = StorageHelper.getOutputFolder(project);
		if (!outputFolder.exists()) {
			return;
		}
		IFile buildFile = outputFolder.getFile(BUILD_FILE);
		if (!buildFile.exists()) {
			return;
		}
		try {
			try (InputStream inputStream = buildFile.getContents()) {
					StepDefinitionsRepository repository = StorageHelper.fromStream(StepDefinitionsRepository.class,
							inputStream, monitor);
					this.add(project, repository);
			}
			//If an error occures then this means our stored data is incompatible and we must start with a fresh repository
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "loading StepDefinitionStore failed, a full rebuild of the project might be required", e));
			this.add(project, new StepDefinitionsRepository());
		} catch (ClassNotFoundException e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "loading StepDefinitionStore failed, a full rebuild of the project might be required", e));
			this.add(project, new StepDefinitionsRepository());
		}
	}

}
