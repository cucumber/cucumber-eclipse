package io.cucumber.eclipse.editor.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import io.cucumber.eclipse.editor.Images;

public class CucumberPropertiesPage extends PropertyPage {

	private static final String NAMESPACE = "io.cucumber.eclipse.editor";
	private Text validationPlugins;

	public CucumberPropertiesPage() {
		setTitle("Cucumber");
		setDescription("You can configure Cucumber related properties for your project here");
		setImageDescriptor(Images.getCukesIconDescriptor());
		noDefaultAndApplyButton();
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
		return composite;
	}

	private IResource getResource() {
		return getElement().getAdapter(IResource.class);
	}

	public static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(NAMESPACE);
		return node;
	}

}