package cucumber.eclipse.launching;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class CucumberMainTab extends SharedJavaMainTab implements ILaunchConfigurationTab {



	protected Text featurePathText;
	protected Text gluePathText;
	private WidgetListener listener = new WidgetListener();
	private Button featureButton;
	private Button glueButton;

	private class WidgetListener implements ModifyListener, SelectionListener {

		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {/* do nothing */
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == featureButton) {
				// TODO
			} else if (source == glueButton) {
				// TODO
			} else
				updateLaunchConfigurationDialog();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		createProjectEditor(comp);
		setControl(comp);
		createFeaturePathEditor(comp);
		createGluePathEditor(comp);

	}

	private void createGluePathEditor(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Glue:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		gluePathText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);

		gluePathText.setLayoutData(gd);
		gluePathText.setFont(font);

		gluePathText.addModifyListener(listener);

		glueButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null);
		glueButton.addSelectionListener(listener);
	}

	private void createFeaturePathEditor(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Feature Path:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		featurePathText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);

		featurePathText.setLayoutData(gd);
		featurePathText.setFont(font);

		featurePathText.addModifyListener(listener);

		featureButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null);
		featureButton.addSelectionListener(listener);
	}

	@Override
	public String getName() {
		return CucumberFeatureLaunchConstants.CUCUMBER_FEATURE_RUNNER;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePathText.getText().trim());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePathText.getText().trim());
		mapResources(config);

	}

	private String getDefaultGluePath() {
		return CucumberFeatureLaunchConstants.DEFAULT_CLASSPATH;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

		IProject javaProject = CucumberFeaureLaunchUtils.getProject();
		String featurePath = CucumberFeaureLaunchUtils.getFeaturePath();
		String gluePath = getDefaultGluePath();
		if (javaProject != null && CucumberFeaureLaunchUtils.getFeaturePath() != null) {
			initializeCucumberProject(gluePath, featurePath, javaProject, config);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, "");
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, "");
		}

	}

	@Override
	protected void handleSearchButtonSelected() {

	}

	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateFeaturePathFromConfig(config);
		updateGluePathFromConfig(config);
	}

	private void updateGluePathFromConfig(ILaunchConfiguration config) {
		CucumberFeaureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePathText);
	}

	private void updateFeaturePathFromConfig(ILaunchConfiguration config) {
		CucumberFeaureLaunchUtils.updateFromConfig(config, CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePathText);
	}



	protected void initializeCucumberProject(String gluePath, String featurePath, IProject javaProject, ILaunchConfigurationWorkingCopy config) {
		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getName();
		}

		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePath);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePath);
	}

}
