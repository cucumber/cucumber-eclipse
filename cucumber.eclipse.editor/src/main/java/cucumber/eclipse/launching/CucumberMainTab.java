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

	private static final String ATTR_FEATURE_PATH = "cucumber feature";
	private static final String ATTR_GLUE_PATH = "glue path";
	protected Text fFeaturePath;
	protected Text fGluePath;
	private WidgetListener fListener = new WidgetListener();
	private Button fFeatureButton;
	private Button fGlueButton;

	private class WidgetListener implements ModifyListener, SelectionListener {

		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {/* do nothing */
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fFeatureButton) {
				// TODO
			} else if (source == fGlueButton) {
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
		fGluePath = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);

		fGluePath.setLayoutData(gd);
		fGluePath.setFont(font);

		fGluePath.addModifyListener(fListener);

		fGlueButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null);
		fGlueButton.addSelectionListener(fListener);
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
		fFeaturePath = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);

		fFeaturePath.setLayoutData(gd);
		fFeaturePath.setFont(font);

		fFeaturePath.addModifyListener(fListener);

		fFeatureButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null);
		fFeatureButton.addSelectionListener(fListener);
	}

	@Override
	public String getName() {
		return "Cucumber Feature Runner";
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute(ATTR_FEATURE_PATH, fFeaturePath.getText().trim());
		config.setAttribute(ATTR_GLUE_PATH, fGluePath.getText().trim());
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

	private TextSelection getTextSelection() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelectionService service = window.getSelectionService();
		
		if (service instanceof TextSelection) return  (TextSelection)  service.getSelection();
		else return null;
	}

	private String getDefaultGluePath() {
		return "classpath:";
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

		IProject javaProject = getProject();
		String featurePath = getFeaturePath();
		String gluePath = getDefaultGluePath();
		if (javaProject != null && getFeaturePath() != null) {
			initializeCucumberProject(gluePath, featurePath, javaProject, config);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
			config.setAttribute(ATTR_FEATURE_PATH, EMPTY_STRING);
			config.setAttribute(ATTR_GLUE_PATH, EMPTY_STRING);
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
		updateFromConfig(config, ATTR_GLUE_PATH, fGluePath);
	}

	private void updateFeaturePathFromConfig(ILaunchConfiguration config) {
		updateFromConfig(config, ATTR_FEATURE_PATH, fFeaturePath);
	}

	private void updateFromConfig(ILaunchConfiguration config, String attrib, Text text) {
		String s = EMPTY_STRING;
		try {
			s = config.getAttribute(attrib, EMPTY_STRING);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		text.setText(s);
	}

	protected void initializeCucumberProject(String gluePath, String featurePath, IProject javaProject, ILaunchConfigurationWorkingCopy config) {

		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getName();
		}

		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
		System.out.println("Settting ....................... " + featurePath);
		config.setAttribute(ATTR_FEATURE_PATH, featurePath);
		config.setAttribute(ATTR_GLUE_PATH, gluePath);
	}

}
