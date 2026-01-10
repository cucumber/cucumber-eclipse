package io.cucumber.eclipse.editor.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import io.cucumber.eclipse.editor.Images;
import io.cucumber.eclipse.editor.launching.Mode;
import io.cucumber.eclipse.editor.preferences.CucumberPreferencePage;
import io.cucumber.eclipse.editor.preferences.EditorReconciler;

public class CucumberPropertiesPage extends PropertyPage {

	private Map<Mode, Button> modeButtons = new HashMap<>();
	private Button enableProjectSpecific;

	public CucumberPropertiesPage() {
		setTitle("Cucumber");
		setDescription("You can configure Cucumber related properties for your project here");
		setImageDescriptor(Images.getCukesIconDescriptor());
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
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
		for (Mode mode : Mode.values()) {
			Button button = new Button(composite, SWT.CHECK);
			button.setSelection(node.getBoolean(CucumberEditorProperties.KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), true));
			button.setText(CucumberPreferencePage.getLabelForMode(mode));
			modeButtons.put(mode, button);
		}
		updateUI();
		return composite;
	}

	protected void updateUI() {
		boolean enable = enableProjectSpecific.getSelection();
		for (Button b : modeButtons.values()) {
			b.setEnabled(enable);
		}
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
		CucumberEditorProperties properties = new CucumberEditorProperties(node);
		properties.flush();
		EditorReconciler.reconcileAllFeatureEditors();
		return true;
	}

}