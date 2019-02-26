package cucumber.eclipse.editor.properties;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

public class CucumberPropertiesPage extends PropertyPage {

	public CucumberPropertiesPage() {
		super();
	}


	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		Label label = new Label(composite, SWT.WRAP);
		label.setText("You can configure Cucumber related properties for your project here");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}



}