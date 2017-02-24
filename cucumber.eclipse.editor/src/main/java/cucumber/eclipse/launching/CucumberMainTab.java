package cucumber.eclipse.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class CucumberMainTab extends SharedJavaMainTab implements ILaunchConfigurationTab {



	protected Text featurePathText;
	protected Text gluePathText;
	private WidgetListener listener = new WidgetListener();
	private Button featureButton;
	private Button glueButton;
	private Button monochromeCheckbox;
	private Button prettyCheckbox;
	private Button jsonCheckbox;
	private Button htmlCheckbox;
	private Button progressCheckbox;
	private Button usageCheckbox;
	private Button junitCheckbox;
	private Button rerunCheckbox;


	private class WidgetListener implements ModifyListener, SelectionListener {

		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {/* do nothing */
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == featureButton) {
			    // File standard dialog
			    FileDialog fileDialog = new FileDialog(getShell());
			    // Set the text
			    fileDialog.setText("Select File");
			    // Set filter on .txt files
			    fileDialog.setFilterExtensions(new String[] { "*.feature" });
			    // Put in a readable name for the filter
			    fileDialog.setFilterNames(new String[] { "Features(*.feature)" });
			    // Open Dialog and save result of selection
			    fileDialog.setFileName(featurePathText.getText());
			    featurePathText.setText( fileDialog.open() );
			    //System.out.println(selected);
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
		createFormatterOptions(comp);

	}

	private void createFormatterOptions(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Formatters:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		
		monochromeCheckbox=new Button(group,SWT.CHECK);
		monochromeCheckbox.addSelectionListener(listener);
		monochromeCheckbox.setText("monochrome");
		
		prettyCheckbox=new Button(group,SWT.CHECK);
		prettyCheckbox.addSelectionListener(listener);
		prettyCheckbox.setText("pretty");
		
		jsonCheckbox=new Button(group,SWT.CHECK);
		jsonCheckbox.addSelectionListener(listener);
		jsonCheckbox.setText("JSON");
		
		progressCheckbox=new Button(group,SWT.CHECK);
		progressCheckbox.addSelectionListener(listener);
		progressCheckbox.setText("progress");
		
		rerunCheckbox=new Button(group,SWT.CHECK);
		rerunCheckbox.addSelectionListener(listener);
		rerunCheckbox.setText("rerun");
		
		usageCheckbox=new Button(group,SWT.CHECK);
		usageCheckbox.addSelectionListener(listener);
		usageCheckbox.setText("usage");

		// Need to add option to choose path before can enable
		
		htmlCheckbox=new Button(group,SWT.CHECK);
		htmlCheckbox.addSelectionListener(listener);
		htmlCheckbox.setText("HTML");
		htmlCheckbox.setVisible(false);
		
		junitCheckbox=new Button(group,SWT.CHECK);
		junitCheckbox.addSelectionListener(listener);
		junitCheckbox.setText("JUnit");
		junitCheckbox.setVisible(false);
		
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

//		glueButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null);
//		glueButton.addSelectionListener(listener);
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
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, monochromeCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, jsonCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, progressCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY, prettyCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, htmlCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, usageCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, junitCheckbox.getSelection());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, rerunCheckbox.getSelection());
		
		mapResources(config);

	}

	private String getDefaultGluePath() {
		return CucumberFeatureLaunchConstants.DEFAULT_CLASSPATH;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {

		IProject javaProject = CucumberFeatureLaunchUtils.getProject();
		String featurePath = CucumberFeatureLaunchUtils.getFeaturePath();
		String gluePath = getDefaultGluePath();
		
		if (javaProject != null && CucumberFeatureLaunchUtils.getFeaturePath() != null) {
			initializeCucumberProject(gluePath, featurePath, javaProject, config);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, "");
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, "");
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, true);
			config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, true);
		}

	}

	@Override
	protected void handleSearchButtonSelected() {

	}

	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateFeaturePathFromConfig(config);
		updateGluePathFromConfig(config);
		updateFormattersFromConfig(config);
	}

	private void updateFormattersFromConfig(ILaunchConfiguration config) {		
		monochromeCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME));
		jsonCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_JSON));
		junitCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_JUNIT));
		prettyCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_PRETTY));
		progressCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS));
		htmlCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_HTML));
		usageCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_USAGE));
		rerunCheckbox.setSelection(CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_IS_RERUN));
		
	}

	private void updateGluePathFromConfig(ILaunchConfiguration config) {
		CucumberFeatureLaunchUtils.updateFromConfig(config,CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePathText);
	}

	private void updateFeaturePathFromConfig(ILaunchConfiguration config) {
		CucumberFeatureLaunchUtils.updateFromConfig(config, CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePathText);
	}



	protected void initializeCucumberProject(String gluePath, String featurePath, IProject javaProject, ILaunchConfigurationWorkingCopy config) {
		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getName();
		}

		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePath);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePath);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, true);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY, true);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, false);
	}

}
