package cucumber.eclipse.editor.steps;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IFileEditorInput;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.editors.Editor;

/**
 * Each project have its own scope of step definitions defined by its own
 * resources or child resources. So a project have its own glue.
 * 
 * Thus, a step definition can only glue with gherkin step in the same project
 * scope.
 * 
 * @author qvdk
 *
 */
public class GlueStorage implements BuildStorage<GlueRepository> {

	public static final BuildStorage<GlueRepository> INSTANCE = new GlueStorage();

	private static final String BUILD_FILE = "cucumber.glue.tmp";

	private Map<IProject, GlueRepository> glueRepositoryByProject = new HashMap<IProject, GlueRepository>();

	private boolean isInitialized = false;

	private GlueStorage() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cucumber.eclipse.editor.steps.Storage#getOrCreate(org.eclipse.core.
	 * resources. IProject)
	 */
	@Override
	public synchronized GlueRepository getOrCreate(IProject project, IProgressMonitor monitor) throws CoreException {
		GlueRepository glueRepository = this.glueRepositoryByProject.get(project);
		if (glueRepository == null) {
			glueRepository = new GlueRepository(project);
			this.add(project, glueRepository);
		}
		return glueRepository;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cucumber.eclipse.editor.steps.Storage#add(org.eclipse.core.resources.
	 * IProject, cucumber.eclipse.editor.steps.GlueRepository)
	 */
	@Override
	public void add(IProject project, GlueRepository glueRepository) {
		this.glueRepositoryByProject.put(project, glueRepository);
	}

	public static GlueRepository findGlueRepository(Editor editor) throws CoreException {
		IFileEditorInput fileEditorInput = (IFileEditorInput) editor.getEditorInput();
		IProject project = fileEditorInput.getFile().getProject();
		return INSTANCE.getOrCreate(project, null);
	}

	@Override
	public void persist(IProject project, IProgressMonitor monitor) throws CoreException {
		GlueRepository glueRepository = this.glueRepositoryByProject.get(project);
		if (glueRepository == null) {
			return;
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		try {
			try (InputStream stream = StorageHelper.toStream(glueRepository, subMonitor.newChild(50))) {
				StorageHelper.saveIntoBuildDirectory(BUILD_FILE, project, subMonitor.newChild(50), stream);
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
				GlueRepository glueRepository = StorageHelper.fromStream(GlueRepository.class, inputStream, monitor);
				glueRepository.setProject(project);
				glueRepositoryByProject.put(project, glueRepository);
			}
		} catch (RuntimeException e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "loading StepDefinitionStore failed, a full rebuild of the project might be required", e));
			glueRepositoryByProject.put(project, new GlueRepository(project));
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "loading GlueStore failed, a full rebuild of the project might be required", e));
			glueRepositoryByProject.put(project, new GlueRepository(project));
		} catch (ClassNotFoundException e) {
			glueRepositoryByProject.put(project, new GlueRepository(project));
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "loading GlueStore failed, a full rebuild of the project might be required", e));
		}
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

}
