package io.cucumber.eclipse.python.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class CucumberBehaveMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

	protected Text featurePathText;
	protected Text workingDirectoryText;
	protected Text pythonInterpreterText;
	protected Text tagsText;
	private WidgetListener listener = new WidgetListener();
	private Button featureButton;
	private Button workingDirectoryButton;
	private Button verboseCheckbox;
	private Button noCaptureCheckbox;
	private Button dryRunCheckbox;

	private class WidgetListener implements ModifyListener, SelectionListener {

		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == featureButton) {
				FileDialog fileDialog = new FileDialog(getShell());
				fileDialog.setText("Select Feature File");
				fileDialog.setFilterExtensions(new String[] { "*.feature" });
				fileDialog.setFilterNames(new String[] { "Feature Files (*.feature)" });
				fileDialog.setFileName(featurePathText.getText());
				String selected = fileDialog.open();
				if (selected != null) {
					featurePathText.setText(selected);
				}
			} else if (source == workingDirectoryButton) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setText("Select Working Directory");
				directoryDialog.setFilterPath(workingDirectoryText.getText());
				String selected = directoryDialog.open();
				if (selected != null) {
					workingDirectoryText.setText(selected);
				}
			}
			updateLaunchConfigurationDialog();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		createFeaturePathEditor(comp);
		createWorkingDirectoryEditor(comp);
		createPythonInterpreterEditor(comp);
		createTagsEditor(comp);
		createBehaveOptions(comp);
		setControl(comp);
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

		featureButton = createPushButton(group, "Browse...", null);
		featureButton.addSelectionListener(listener);
	}

	private void createWorkingDirectoryEditor(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Working Directory:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		
		workingDirectoryText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		workingDirectoryText.setLayoutData(gd);
		workingDirectoryText.setFont(font);
		workingDirectoryText.addModifyListener(listener);

		workingDirectoryButton = createPushButton(group, "Browse...", null);
		workingDirectoryButton.addSelectionListener(listener);
	}

	private void createPythonInterpreterEditor(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Python Interpreter:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setFont(font);
		
		pythonInterpreterText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pythonInterpreterText.setLayoutData(gd);
		pythonInterpreterText.setFont(font);
		pythonInterpreterText.addModifyListener(listener);
	}

	private void createTagsEditor(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Tags:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setFont(font);
		
		tagsText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		tagsText.setLayoutData(gd);
		tagsText.setFont(font);
		tagsText.addModifyListener(listener);
	}

	private void createBehaveOptions(Composite comp) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText("Behave Options:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);

		verboseCheckbox = new Button(group, SWT.CHECK);
		verboseCheckbox.addSelectionListener(listener);
		verboseCheckbox.setText("Verbose");

		noCaptureCheckbox = new Button(group, SWT.CHECK);
		noCaptureCheckbox.addSelectionListener(listener);
		noCaptureCheckbox.setText("No Capture");

		dryRunCheckbox = new Button(group, SWT.CHECK);
		dryRunCheckbox.addSelectionListener(listener);
		dryRunCheckbox.setText("Dry Run");
	}

	@Override
	public String getName() {
		return "Cucumber Behave Options";
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, featurePathText.getText().trim());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, workingDirectoryText.getText().trim());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_PYTHON_INTERPRETER, pythonInterpreterText.getText().trim());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_TAGS, tagsText.getText().trim());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_IS_VERBOSE, verboseCheckbox.getSelection());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_IS_NO_CAPTURE, noCaptureCheckbox.getSelection());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_IS_DRY_RUN, dryRunCheckbox.getSelection());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		// Set default values
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, "");
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, getDefaultWorkingDirectory());
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_PYTHON_INTERPRETER, "python");
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_TAGS, "");
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_IS_VERBOSE, false);
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_IS_NO_CAPTURE, false);
		config.setAttribute(CucumberBehaveLaunchConstants.ATTR_IS_DRY_RUN, false);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		updateFromConfig(config, CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, featurePathText);
		updateFromConfig(config, CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, workingDirectoryText);
		updateFromConfig(config, CucumberBehaveLaunchConstants.ATTR_PYTHON_INTERPRETER, pythonInterpreterText);
		updateFromConfig(config, CucumberBehaveLaunchConstants.ATTR_TAGS, tagsText);
		
		verboseCheckbox.setSelection(getAttribute(config, CucumberBehaveLaunchConstants.ATTR_IS_VERBOSE, false));
		noCaptureCheckbox.setSelection(getAttribute(config, CucumberBehaveLaunchConstants.ATTR_IS_NO_CAPTURE, false));
		dryRunCheckbox.setSelection(getAttribute(config, CucumberBehaveLaunchConstants.ATTR_IS_DRY_RUN, false));
	}

	private void updateFromConfig(ILaunchConfiguration config, String attribute, Text text) {
		String value = "";
		try {
			value = config.getAttribute(attribute, "");
		} catch (CoreException e) {
			// Use default empty value
		}
		text.setText(value);
	}

	private boolean getAttribute(ILaunchConfiguration config, String attribute, boolean defaultValue) {
		try {
			return config.getAttribute(attribute, defaultValue);
		} catch (CoreException e) {
			return defaultValue;
		}
	}

	private String getDefaultWorkingDirectory() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		if (projects.length > 0) {
			return projects[0].getLocation().toOSString();
		}
		return "";
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		
		String featurePath = featurePathText.getText().trim();
		if (featurePath.isEmpty()) {
			setErrorMessage("Feature path must be specified");
			return false;
		}
		
		String workingDirectory = workingDirectoryText.getText().trim();
		if (workingDirectory.isEmpty()) {
			setErrorMessage("Working directory must be specified");
			return false;
		}
		
		return true;
	}

}
