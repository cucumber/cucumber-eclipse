package cucumber.eclipse.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public class CucumberMainTab extends SharedJavaMainTab implements ILaunchConfigurationTab {

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		createProjectEditor(comp);
		setControl(comp);
		IProject project = getProject();
		if (project != null) {
			fProjText.setText(getProject().getName());
		}
	}

	@Override
	public String getName() {
		return "Cucumber Feature Runner";
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		mapResources(config);

	}

	protected IProject getProject() {

		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
				IFile file = input.getFile();
				IProject activeProject = file.getProject();
				return activeProject;
			}
		}

		return null;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

		IProject javaProject = getProject();
		if (javaProject != null) {
			initializeJavaProject(javaProject, config);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		}

	}

	@Override
	protected void handleSearchButtonSelected() {

	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {

	}

	protected void initializeJavaProject(IProject javaProject, ILaunchConfigurationWorkingCopy config) {

		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getName();
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);

	}

}
