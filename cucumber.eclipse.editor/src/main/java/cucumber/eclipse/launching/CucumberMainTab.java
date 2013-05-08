package cucumber.eclipse.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public class CucumberMainTab extends SharedJavaMainTab implements ILaunchConfigurationTab {

	protected Text fFeaturePath;

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		createProjectEditor(comp);
		setControl(comp);

		Font font = parent.getFont();
		String text = "lorem";
		Group mainGroup = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_HORIZONTAL);
		Composite comp2 = SWTFactory.createComposite(mainGroup, font, 2, 2, GridData.FILL_BOTH, 0, 0);
		fFeaturePath = SWTFactory.createSingleText(comp2, 1);
		fFeaturePath.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

	}

	@Override
	public String getName() {
		return "Cucumber Feature Runner";
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute("cucumber feature", fFeaturePath.getText().trim());
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

	protected String getFeaturePath() {

		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
				return input.getFile().getLocation().toString();
			}
		}

		return null;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

		IProject javaProject = getProject();
		String featurePath = getFeaturePath();
		if (javaProject != null && getFeaturePath()!=null) {
			initializeCucumberProject(featurePath, javaProject, config);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
			config.setAttribute("cucumber feature", EMPTY_STRING);
		}

	}

	@Override
	protected void handleSearchButtonSelected() {

	}

	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateFeaturePathFromConfig(config);
	}

	private void updateFeaturePathFromConfig(ILaunchConfiguration config) {
		String featurePath = "";
		try {
			featurePath = config.getAttribute("cucumber feature", "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		fFeaturePath.setText(featurePath);
	}

	protected void initializeCucumberProject(String featurePath, IProject javaProject, ILaunchConfigurationWorkingCopy config) {

		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getName();
		}

		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
		System.out.println("Settting ....................... " + featurePath);
		config.setAttribute("cucumber feature", featurePath);

	}

}
