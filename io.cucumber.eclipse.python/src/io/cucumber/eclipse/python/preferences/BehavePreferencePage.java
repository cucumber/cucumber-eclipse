package io.cucumber.eclipse.python.preferences;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for Behave backend configuration
 */
public class BehavePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID = "io.cucumber.eclipse.python.preferences.BehavePreferencePage";
	
	private Text behaveCommandText;
	private BehavePreferences behavePreferences;
	
	public BehavePreferencePage() {
		super();
		behavePreferences = BehavePreferences.of();
		setPreferenceStore(behavePreferences.store());
		setTitle("Behave Backend");
		setDescription("Configure Python Behave backend settings for Cucumber Eclipse");
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		
		// Behave command configuration
		Label commandLabel = new Label(composite, SWT.NONE);
		commandLabel.setText("Behave Command:");
		commandLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		
		behaveCommandText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		behaveCommandText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		behaveCommandText.setText(behavePreferences.behaveCommand());
		
		Label hintLabel = new Label(composite, SWT.WRAP);
		hintLabel.setText("Specify the command to launch behave (e.g., 'behave', '/usr/bin/behave', or 'python -m behave').\nDefault is 'behave'.");
		GridData hintData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		hintData.widthHint = 400;
		hintLabel.setLayoutData(hintData);
		
		return composite;
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// Nothing to initialize
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		behaveCommandText.setText(BehavePreferences.DEFAULT_BEHAVE_COMMAND);
	}
	
	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		if (store != null) {
			store.setValue(BehavePreferences.PREF_BEHAVE_COMMAND, behaveCommandText.getText().trim());
		}
		return super.performOk();
	}
}
