package io.cucumber.eclipse.java.preferences;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.cucumber.eclipse.java.Activator;
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
	private GlueCodePackageTable glueCodePackageTable;

	/**
	 * Constructor
	 */
	public CucumberJavaPreferencesPage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setTitle(CucumberJavaUIMessages.CucumberJavaPreferencesPage__title);
		setDescription(CucumberJavaUIMessages.CucumberJavaPreferencesPage__description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.
	 * widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
//				IJavaDebugHelpContextIds.JAVA_STEP_FILTER_PREFERENCE_PAGE);
		// The main composite
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		createStepFilterPreferences(composite);
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/**
	 * Create a group to contain the step filter related widgetry
	 */
	private void createStepFilterPreferences(Composite parent) {
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
//		fUseStepDefinitionsFiltersButton = SWTFactory.createCheckButton(container,
//		CucumberJavaUIMessages.CucumberJavaPreferencesPage__Use_packages_filters, null,
//		CucumberJavaPreferences.isUseStepDefinitionsFilters(), 2);
//fUseStepDefinitionsFiltersButton.addSelectionListener(new SelectionListener() {
//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		setPageEnablement(fUseStepDefinitionsFiltersButton.getSelection());
//	}
//
//	@Override
//	public void widgetDefaultSelected(SelectionEvent e) {
//	}
//});
//SWTFactory.createLabel(container,
//		CucumberJavaUIMessages.CucumberJavaPreferencesPage__Define_step_definitions_filters, 2);
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

//		setPageEnablement(fUseStepDefinitionsFiltersButton.getSelection());
		showHookAnnotations = new Button(container, SWT.CHECK);
		showHookAnnotations.setSelection(CucumberJavaPreferences.showHooks());
		showHookAnnotations.setText("Show Hook Annotations in Feature files");
	}

//	/**
//	 * Enables or disables the widgets on the page, with the exception of
//	 * <code>fUseStepFiltersButton</code> according to the passed boolean
//	 * 
//	 * @param enabled the new enablement status of the page's widgets
//	 * @since 3.2
//	 */
//	protected void setPageEnablement(boolean enabled) {
////		fAddFilterButton.setEnabled(enabled);
//		fAddPackageButton.setEnabled(enabled);
////		fAddTypeButton.setEnabled(enabled);
//		fDeselectAllButton.setEnabled(enabled);
//		fSelectAllButton.setEnabled(enabled);
//		fTableViewer.getTable().setEnabled(enabled);
//		fRemoveFilterButton.setEnabled(enabled & !fTableViewer.getSelection().isEmpty());
//	}



	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		
		IPreferenceStore store = getPreferenceStore();
		FilterStrings filterStrings = glueCodePackageTable.getFilters();
		store.setValue(CucumberJavaPreferences.PREF_ACTIVE_FILTERS_LIST, filterStrings.active());
		store.setValue(CucumberJavaPreferences.PREF_INACTIVE_FILTERS_LIST, filterStrings.inactive());

		store.setValue(CucumberJavaPreferences.PREF_SHOW_HOOK_ANNOTATIONS, showHookAnnotations.getSelection());
		return super.performOk();
	}

//	private String prefixPreferences(String preference) {
//		return Activator.PLUGIN_ID + "." + preference;
//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		boolean stepenabled = CucumberJavaPreferences.isUseStepDefinitionsFilters();
//		fUseStepDefinitionsFiltersButton.setSelection(stepenabled);
//		setPageEnablement(stepenabled);
		glueCodePackageTable.performDefaults();
		showHookAnnotations.setSelection(
				getPreferenceStore().getDefaultBoolean(CucumberJavaPreferences.PREF_SHOW_HOOK_ANNOTATIONS));
		super.performDefaults();
	}

}
