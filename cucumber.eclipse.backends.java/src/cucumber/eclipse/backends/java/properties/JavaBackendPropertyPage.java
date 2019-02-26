package cucumber.eclipse.backends.java.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
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

public class JavaBackendPropertyPage extends PropertyPage {

	private static final String NAMESPACE = "cucumber.backend.java";
	public static final QualifiedName KEY_ENABLE = new QualifiedName(NAMESPACE, "enable");
	public static final QualifiedName KEY_GLUE = new QualifiedName(NAMESPACE, "glue");
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
		button.setSelection(isBackendEnabled((IResource) getElement()));
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

	public static boolean isBackendEnabled(IResource resource) {
		try {
			return Boolean.parseBoolean(resource.getProject().getPersistentProperty(KEY_ENABLE));
		} catch (CoreException e) {
			return false;
		}
	}

	public static String getGlueOption(IResource resource) {
		try {
			String property = resource.getProject().getPersistentProperty(KEY_GLUE);
			if (property != null) {
				return property;
			}
		} catch (CoreException e) {
			//
		}
		return "";
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
		text.setText(getGlueOption((IResource) getElement()));
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
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(KEY_GLUE, glueOption.getText());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}