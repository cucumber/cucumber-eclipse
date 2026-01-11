package io.cucumber.eclipse.editor.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import io.cucumber.eclipse.editor.Images;
import io.cucumber.eclipse.editor.launching.Mode;
import io.cucumber.eclipse.editor.preferences.CucumberEditorPreferences;
import io.cucumber.eclipse.editor.preferences.CucumberPreferencePage;
import io.cucumber.eclipse.editor.preferences.EditorReconciler;

public class CucumberPropertiesPage extends PropertyPage {

	private Map<Mode, Button> modeButtons = new HashMap<>();
	private Button enableProjectSpecific;
	private Text validationTimeoutText;
	private Label timeoutLabel;

	public CucumberPropertiesPage() {
		setTitle("Cucumber");
		setDescription("You can configure Cucumber related properties for your project here");
		setImageDescriptor(Images.getCukesIconDescriptor());
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		IResource resource = getResource();
		IEclipsePreferences node = CucumberEditorProperties.getNode(resource);
		
		enableProjectSpecific = new Button(composite, SWT.CHECK);
		enableProjectSpecific.setText("Enable project specific settings");
		enableProjectSpecific.setSelection(node.getBoolean(CucumberEditorProperties.KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, false));
		enableProjectSpecific.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateUI();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		GridData enableData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		enableData.horizontalSpan = 2;
		enableProjectSpecific.setLayoutData(enableData);
		
		for (Mode mode : Mode.values()) {
			Button button = new Button(composite, SWT.CHECK);
			button.setSelection(node.getBoolean(CucumberEditorProperties.KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), true));
			button.setText(CucumberPreferencePage.getLabelForMode(mode));
			GridData buttonData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			buttonData.horizontalSpan = 2;
			button.setLayoutData(buttonData);
			modeButtons.put(mode, button);
		}
		
		timeoutLabel = new Label(composite, SWT.NONE);
		timeoutLabel.setText("Validation timeout (ms):");
		timeoutLabel.setToolTipText("Time to wait after typing before validating feature files (milliseconds)");
		
		validationTimeoutText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		GridData timeoutData = new GridData(100, SWT.DEFAULT);
		validationTimeoutText.setLayoutData(timeoutData);
		validationTimeoutText.setText(String.valueOf(node.getInt(CucumberEditorProperties.KEY_VALIDATION_TIMEOUT, 
				CucumberEditorPreferences.DEFAULT_VALIDATION_TIMEOUT)));
		validationTimeoutText.setToolTipText("Time to wait after typing before validating feature files (milliseconds)");
		
		updateUI();
		return composite;
	}

	protected void updateUI() {
		boolean enable = enableProjectSpecific.getSelection();
		for (Button b : modeButtons.values()) {
			b.setEnabled(enable);
		}
		validationTimeoutText.setEnabled(enable);
		timeoutLabel.setEnabled(enable);
	}

	private IResource getResource() {
		return getElement().getAdapter(IResource.class);
	}

	@Override
	public boolean performOk() {
		IEclipsePreferences node = CucumberEditorProperties.getNode(getResource());
		node.putBoolean(CucumberEditorProperties.KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, enableProjectSpecific.getSelection());
		for (Entry<Mode, Button> entry : modeButtons.entrySet()) {
			node.putBoolean(CucumberEditorProperties.KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + entry.getKey().name(), entry.getValue().getSelection());
		}
		
		// Save validation timeout
		try {
			int timeout = Integer.parseInt(validationTimeoutText.getText().trim());
			if (timeout > 0) {
				node.putInt(CucumberEditorProperties.KEY_VALIDATION_TIMEOUT, timeout);
			} else {
				setErrorMessage("Validation timeout must be a positive number");
				return false;
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Validation timeout must be a valid number");
			return false;
		}
		
		CucumberEditorProperties properties = new CucumberEditorProperties(node);
		properties.flush();
		EditorReconciler.reconcileAllFeatureEditors();
		return true;
	}

}