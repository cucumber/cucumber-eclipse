package io.cucumber.eclipse.editor.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import io.cucumber.eclipse.editor.Images;
import io.cucumber.eclipse.editor.launching.Mode;
import io.cucumber.eclipse.editor.preferences.CucumberPreferencePage;

public class CucumberPropertiesPage extends PropertyPage {

	private Text validationPlugins;
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
		CucumberEditorProperties properties = CucumberEditorProperties.of(getResource());
		enableProjectSpecific = new Button(composite, SWT.CHECK);
		enableProjectSpecific.setText("Enable project specific settings");
		enableProjectSpecific.setSelection(properties.isEnabled());
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
			button.setSelection(properties.isShowShortcutFor(mode));
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
		CucumberEditorProperties properties = CucumberEditorProperties.of(getResource());
		properties.setEnabled(enableProjectSpecific.getSelection());
		for (Entry<Mode, Button> entry : modeButtons.entrySet()) {
			properties.setShowShortcutFor(entry.getKey(), entry.getValue().getSelection());
		}
		properties.flush();
		return true;
	}

}