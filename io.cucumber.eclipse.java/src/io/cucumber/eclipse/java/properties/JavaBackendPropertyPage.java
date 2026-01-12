package io.cucumber.eclipse.java.properties;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.BorderData;
import org.eclipse.swt.layout.BorderLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import io.cucumber.eclipse.editor.EditorReconciler;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferencesPage;
import io.cucumber.eclipse.java.preferences.GlueCodePackageTable;
import io.cucumber.eclipse.java.preferences.GlueCodePackageTable.FilterStrings;

public class JavaBackendPropertyPage extends PropertyPage {

	private Text validationPlugins;
	private Button enableProjectSpecific;
	private GlueCodePackageTable glueCodePackageTable;
	private Button hookButton;

	public JavaBackendPropertyPage() {
		setTitle("Cucumber Java Options");
		setDescription("Here you can configure Java related Cucumber options");
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		addValidationOption(composite);
		return composite;
	}

	@SuppressWarnings("restriction")
	private void addValidationOption(Composite parent) {
		IResource resource = getResource();
		IEclipsePreferences node = CucumberJavaBackendProperties.getNode(resource);
		Label label = new Label(parent, SWT.NONE);
		label.setText("Validation Plugins ");
		label.setToolTipText(
				"A comma seperated list of plugins that should be used for validation regardless of feature settings");
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new BorderLayout());
		validationPlugins = new Text(composite, SWT.BORDER);
		validationPlugins.setLayoutData(new BorderData(SWT.CENTER));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);
		validationPlugins.setText(node.get(CucumberJavaBackendProperties.KEY_VALIDATION_PLUGINS, ""));
		Button button = new Button(composite, SWT.PUSH);
		button.setText("Add");
		button.setLayoutData(new BorderData(SWT.RIGHT));
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				OpenTypeSelectionDialog dialog = new OpenTypeSelectionDialog(parent.getShell(), false,
						PlatformUI.getWorkbench().getProgressService(), SearchEngine.createWorkspaceScope(),
						IJavaSearchConstants.TYPE);

				dialog.setTitle(ActionMessages.OpenTypeInHierarchyAction_dialogTitle);
				dialog.setMessage(ActionMessages.OpenTypeInHierarchyAction_dialogMessage);
				int result = dialog.open();
				if (result != IDialogConstants.OK_ID)
					return;

				Object[] types = dialog.getResult();
				if (types != null && types.length > 0) {
					validationPlugins
							.setText(Stream
									.concat(CucumberJavaBackendProperties.parseList(validationPlugins.getText()),
											Stream.of(types).filter(IType.class::isInstance).map(IType.class::cast)
													.map(IType::getFullyQualifiedName))
									.collect(joinPlugins()));
				}

			}


			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
		labelData.horizontalSpan = 2;
		enableProjectSpecific = new Button(parent, SWT.CHECK);
		enableProjectSpecific.setText("Enable project specific settings");
		enableProjectSpecific.setSelection(node.getBoolean(CucumberJavaBackendProperties.KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, false));
		enableProjectSpecific.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateUI();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		enableProjectSpecific.setLayoutData(labelData);
		new Label(parent, SWT.SEPARATOR).setLayoutData(labelData);
		glueCodePackageTable = new GlueCodePackageTable(parent) {

			@Override
			protected String getFilter(boolean active, boolean defaults) {
				if (defaults) {
					return "";
				}
					if (active) {
						return node.get(CucumberJavaBackendProperties.KEY_ACTIVE_FILTER, "");
				} else {
					return node.get(CucumberJavaBackendProperties.KEY_INACTIVE_FILTER, "");
					}
			}
		};
		hookButton = CucumberJavaPreferencesPage.createHookButton(parent,
				node.getBoolean(CucumberJavaBackendProperties.KEY_SHOW_HOOK, false));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		hookButton.setLayoutData(gd);
		((GridData) glueCodePackageTable.getControl().getLayoutData()).horizontalSpan = 2;
		
		updateUI();
	}

	private void updateUI() {
		boolean enable = enableProjectSpecific.getSelection();
		glueCodePackageTable.setEnabled(enable);
		hookButton.setEnabled(enable);
	}

	private IResource getResource() {
		return getElement().getAdapter(IResource.class);
	}

	private static Collector<CharSequence, ?, String> joinPlugins() {
		return Collectors.joining(", ");
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		validationPlugins.setText("");
		glueCodePackageTable.performDefaults();
		hookButton.setSelection(false);
	}

	@Override
	public boolean performOk() {
		IEclipsePreferences node = CucumberJavaBackendProperties.getNode(getResource());
		node.put(CucumberJavaBackendProperties.KEY_VALIDATION_PLUGINS, validationPlugins.getText());
		node.putBoolean(CucumberJavaBackendProperties.KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, enableProjectSpecific.getSelection());
		FilterStrings filters = glueCodePackageTable.getFilters();
		node.put(CucumberJavaBackendProperties.KEY_ACTIVE_FILTER, filters.active());
		node.put(CucumberJavaBackendProperties.KEY_INACTIVE_FILTER, filters.inactive());
		node.putBoolean(CucumberJavaBackendProperties.KEY_SHOW_HOOK, hookButton.getSelection());
		
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
		
		EditorReconciler.reconcileAllFeatureEditors();
		return true;
	}

}
