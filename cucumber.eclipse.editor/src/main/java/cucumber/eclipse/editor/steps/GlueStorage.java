package cucumber.eclipse.editor.steps;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IFileEditorInput;

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
public class GlueStorage implements Storage<GlueRepository> {

	public static final Storage<GlueRepository> INSTANCE = new GlueStorage();

	private Map<IProject, GlueRepository> glueRepositoryByProject = new HashMap<IProject, GlueRepository>();

	private GlueStorage() {
	}

	/* (non-Javadoc)
	 * @see cucumber.eclipse.editor.steps.Storage#getOrCreate(org.eclipse.core.resources.IProject)
	 */
	@Override
	public GlueRepository getOrCreate(IProject project) {
		GlueRepository glueRepository = this.glueRepositoryByProject.get(project);
		if (glueRepository == null) {
			glueRepository = new GlueRepository();
			this.add(project, glueRepository);
		}
		return glueRepository;
	};

	/* (non-Javadoc)
	 * @see cucumber.eclipse.editor.steps.Storage#add(org.eclipse.core.resources.IProject, cucumber.eclipse.editor.steps.GlueRepository)
	 */
	@Override
	public void add(IProject project, GlueRepository glueRepository) {
		this.glueRepositoryByProject.put(project, glueRepository);
	}

	public static GlueRepository findGlueRepository(Editor editor) {
		IFileEditorInput fileEditorInput = (IFileEditorInput) editor.getEditorInput();
		IProject project = fileEditorInput.getFile().getProject();
		return INSTANCE.getOrCreate(project);
	}
}
