package io.cucumber.eclipse.java.properties;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
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

public class JavaBackendPropertyPage extends PropertyPage {

	private static final String KEY_VALIDATION_PLUGINS = "validationPlugins";
	private static final String NAMESPACE = "cucumber.backend.java";
	private Text validationPlugins;

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
		validationPlugins.setText(getValidationPluginsOption(getResource()).collect(joinPlugins()));
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
									.concat(parsePlugins(validationPlugins.getText()),
											Stream.of(types).filter(IType.class::isInstance).map(IType.class::cast)
													.map(IType::getFullyQualifiedName))
									.collect(joinPlugins()));
				}

			}


			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

	}

	private IResource getResource() {
		return getElement().getAdapter(IResource.class);
	}

	private static Collector<CharSequence, ?, String> joinPlugins() {
		return Collectors.joining(", ");
	}

	public static Stream<String> getValidationPluginsOption(IResource resource) {
		if (resource == null) {
			return Stream.empty();
		}
		IEclipsePreferences node = getNode(resource);
		String string = node.get(KEY_VALIDATION_PLUGINS, "");
		return parsePlugins(string);
	}

	private static Stream<String> parsePlugins(String string) {
		return Arrays.stream(string.split(",")).map(String::trim).filter(Predicate.not(String::isBlank));
	}

	public static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(NAMESPACE);
		return node;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		validationPlugins.setText("");
	}

	@Override
	public boolean performOk() {
		IEclipsePreferences node = getNode(getResource());
		node.put(KEY_VALIDATION_PLUGINS, validationPlugins.getText());
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
		return true;
	}

}
