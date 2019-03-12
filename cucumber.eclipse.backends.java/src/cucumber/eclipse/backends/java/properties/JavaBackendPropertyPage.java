package cucumber.eclipse.backends.java.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
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
import org.osgi.service.prefs.BackingStoreException;

public class JavaBackendPropertyPage extends PropertyPage {

	private static final String KEY_GLUE = "glue";
	private static final String KEY_ENABLED = "enabled";
	private static final String NAMESPACE = "cucumber.backend.java";
	private Text glueOption;
	private Button enableOption;
	private Label glueLabel;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		createInfo(composite);
		enableOption = createEnableOption(composite);
		glueOption = createGlueOption(composite);
		updateUIState();
		return composite;
	}

	private Button createEnableOption(Composite composite) {
		Button button = new Button(composite, SWT.CHECK);
		GridData gd = new GridData();
		button.setLayoutData(gd);
		gd.horizontalSpan = 2;
		button.setText("Enable Java Backend for project");
		button.setSelection(isBackendEnabled(getResource()));
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateUIState();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		return button;
	}

	private IResource getResource() {
		return getElement().getAdapter(IResource.class);
	}

	public static boolean isBackendEnabled(IResource resource) {
		IEclipsePreferences node = getNode(resource);
		return node.getBoolean(KEY_ENABLED, false);
	}

	public static String getGlueOption(IResource resource) {
		IEclipsePreferences node = getNode(resource);
		return node.get(KEY_GLUE, "");
	}

	private static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(NAMESPACE);
		return node;
	}

	protected void updateUIState() {
		if (enableOption != null && !enableOption.isDisposed()) {
			boolean enabled = enableOption.getSelection();
			glueOption.setEnabled(enabled);
			glueLabel.setEnabled(enabled);
		}
	}

	private Text createGlueOption(Composite composite) {
		glueLabel = new Label(composite, SWT.NONE);
		glueLabel.setText("Glue: ");
		Text text = new Text(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		text.setText(getGlueOption(getResource()));
		text.setToolTipText(
				"The Glue option defines wich packages are scanned for glue code, seperate multiple packages by comma");
		return text;
	}

	private void createInfo(Composite parent) {
		Label label = new Label(parent, SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		label.setText(
				"Here you can configure the Options for the Java Backend, options relate closeley to thos used in the CLI");
		new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(gd);
	}

	protected void performDefaults() {
		super.performDefaults();
		glueOption.setText("");
	}

	public boolean performOk() {
		IEclipsePreferences node = getNode(getResource());
		node.put(KEY_GLUE, glueOption.getText());
		node.putBoolean(KEY_ENABLED, enableOption.getSelection());
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
		return true;
	}

}