package io.cucumber.eclipse.java.preferences;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.cucumber.eclipse.editor.preferences.EditorReconciler;
import io.cucumber.eclipse.java.CucumberJavaUIMessages;
import io.cucumber.eclipse.java.preferences.GlueCodePackageTable.FilterStrings;

/**
 * https://github.com/eclipse/eclipse.jdt.debug/blob/master/org.eclipse.jdt.debug.ui/ui/org/eclipse/jdt/internal/debug/ui/JavaStepFilterPreferencePage.java
 * 
 * @author qvdk
 *
 */
public class CucumberJavaPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID = "cucumber.eclipse.editor.steps.jdt.CucumberJavaPreferencesPage"; //$NON-NLS-1$

	private Button showHookAnnotations;
	private Text validationTimeoutText;
	private GlueCodePackageTable glueCodePackageTable;

	private CucumberJavaPreferences javaPreferences;

	/**
	 * Constructor
	 */
	public CucumberJavaPreferencesPage() {
		super();
		javaPreferences = CucumberJavaPreferences.of();
		setPreferenceStore(javaPreferences.store());
		setTitle(CucumberJavaUIMessages.CucumberJavaPreferencesPage__title);
		setDescription(CucumberJavaUIMessages.CucumberJavaPreferencesPage__description);
	}

	@Override
	protected Control createContents(Composite parent) {
		// The main composite
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		createStepFilterPreferences(composite);
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	/**
	 * Create a group to contain the step filter related widgetry
	 */
	private void createStepFilterPreferences(Composite parent) {
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		glueCodePackageTable = new GlueCodePackageTable(container) {

			@Override
			protected String getFilter(boolean active, boolean defaults) {
				IPreferenceStore store = getPreferenceStore();
				if (active) {
					if (defaults) {
						return store.getDefaultString(CucumberJavaPreferences.PREF_ACTIVE_FILTERS_LIST);
					} else {
						return store.getString(CucumberJavaPreferences.PREF_ACTIVE_FILTERS_LIST);
					}
				} else {
					if (defaults) {
						return store.getDefaultString(CucumberJavaPreferences.PREF_INACTIVE_FILTERS_LIST);
					} else {
						return store.getString(CucumberJavaPreferences.PREF_INACTIVE_FILTERS_LIST);
					}
				}
			}

		};

		showHookAnnotations = createHookButton(container, javaPreferences.showHooks());
		
		// Validation timeout field
		Composite timeoutComposite = SWTFactory.createComposite(container, parent.getFont(), 2, 1, GridData.FILL_HORIZONTAL, 0, 0);
		Label timeoutLabel = new Label(timeoutComposite, SWT.NONE);
		timeoutLabel.setText(CucumberJavaUIMessages.CucumberJavaPreferencesPage__validation_timeout_label);
		timeoutLabel.setToolTipText(CucumberJavaUIMessages.CucumberJavaPreferencesPage__validation_timeout_description);
		
		validationTimeoutText = new Text(timeoutComposite, SWT.BORDER | SWT.SINGLE);
		validationTimeoutText.setLayoutData(new GridData(100, SWT.DEFAULT));
		validationTimeoutText.setText(String.valueOf(javaPreferences.validationTimeout()));
		validationTimeoutText.setToolTipText(CucumberJavaUIMessages.CucumberJavaPreferencesPage__validation_timeout_description);

	}

	public static Button createHookButton(Composite container, boolean initialValue) {
		Button btn = new Button(container, SWT.CHECK);
		btn.setText("Show Hook Annotations in Feature files");
		btn.setSelection(initialValue);
		return btn;
	}

	@Override
	public boolean performOk() {
		
		FilterStrings filterStrings = glueCodePackageTable.getFilters();
		getPreferenceStore().setValue(CucumberJavaPreferences.PREF_ACTIVE_FILTERS_LIST, filterStrings.active());
		getPreferenceStore().setValue(CucumberJavaPreferences.PREF_INACTIVE_FILTERS_LIST, filterStrings.inactive());

		CucumberJavaPreferences.setShowHooks(getPreferenceStore(), showHookAnnotations.getSelection());
		
		// Save validation timeout
		try {
			int timeout = Integer.parseInt(validationTimeoutText.getText().trim());
			if (timeout > 0) {
				getPreferenceStore().setValue(CucumberJavaPreferences.PREF_VALIDATION_TIMEOUT, timeout);
			} else {
				setErrorMessage("Validation timeout must be a positive number");
				return false;
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Validation timeout must be a valid number");
			return false;
		}
		
		boolean result = super.performOk();
		if (result) {
			EditorReconciler.reconcileAllFeatureEditors();
		}
		return result;
	}

	@Override
	protected void performDefaults() {
		glueCodePackageTable.performDefaults();
		showHookAnnotations.setSelection(
				getPreferenceStore().getDefaultBoolean(CucumberJavaPreferences.PREF_SHOW_HOOK_ANNOTATIONS));
		validationTimeoutText.setText(String.valueOf(CucumberJavaPreferences.DEFAULT_VALIDATION_TIMEOUT));
		super.performDefaults();
	}

}
