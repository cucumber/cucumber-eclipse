package cucumber.eclipse.editor.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import cucumber.eclipse.editor.Activator;

/**
 * @author girija.panda@nokia.com
 * 
 *         Purpose: Creating a New 'User Settings' Preference Page as
 *         Cucumber-Preference. This 'User Settings' Preference allows user to
 *         add/input The Package Name of External Class-Path JAR having step
 *         definitions And All steps can be reused/Populated into the current
 *         project Cucumber Preference Page : Cucumber User Settings Input Field
 *         Name : Add Root Package Name Of JAR File Input Field Value : <Any
 *         Root Package Name> exists in class-path JAR, ex. com.nokia.bdd
 * 
 */

public class CucumberUserSettingsPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Composite composite = null;

	// Description Label
	private CLabel descLabel = null;
	private String description = "Cucumber User Settings";
	private String cucumberImage = "icons/cukes.gif";

	// Package description
	private String groupLabelName = "Root package name(s) of your jar/pom-dependency:";
	private String groupToolTip = "Add Root Package Name(s)[Comma(,) separated] exists in your project class-path as a JAR/POM-Dependency contains all Step-Definitions-files";

	// Text Box
	private Text packageTextBox = null;
	public static String packageNameText = null;

	// Detail Label
	private Label exampleLabel = null;
	private String exampleLabelName = "...(e.g. com.proj1.bdd, org.proj2.bdd,...etc)";

	public CucumberUserSettingsPage() {
		// setTitle(description);
		// setDescription(description);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());

	}

	@Override
	protected Control createContents(Composite parent) {

		composite = new Composite(parent, 0);
		composite.setLayout(new GridLayout(1, false));

		// Description and Image
		descLabel = new CLabel(composite, SWT.NULL);
		descLabel.setAlignment(SWT.LEFT);
		descLabel.setText(getString(description));
		descLabel.setImage(getImage(cucumberImage));

		// Group
		Group packageGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		packageGroup.setLayout(new GridLayout(2, false));
		packageGroup.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
		packageGroup.setText(groupLabelName);
		packageGroup.setToolTipText(groupToolTip);
		// Set font Color
		// packageGroup.setForeground(packageGroup.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));

		// Package Text Box
		packageTextBox = new Text(packageGroup, 2048);
		packageTextBox.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
		packageTextBox.setToolTipText(groupToolTip);
		packageTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				CucumberUserSettingsPage.packageNameText = CucumberUserSettingsPage.this.packageTextBox.getText();
			}
		});
		packageTextBox.pack();
		
		// Example Label
		exampleLabel = new Label(packageGroup, 0);
		exampleLabel.setLayoutData(new GridData(4, 16777216, true, false, 2, 1));
		exampleLabel.setAlignment(SWT.RIGHT);
		exampleLabel.setText(exampleLabelName);
		//Font-Style-Italic
		FontData[] fontData = exampleLabel.getFont().getFontData();
		fontData[0].setStyle(SWT.ITALIC);
		exampleLabel.setFont(new Font(exampleLabel.getDisplay(), fontData[0]));
		exampleLabel.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		exampleLabel.pack();
		

		packageGroup.pack();
		composite.pack();

		// Initialize values
		initializeValues();

		return composite;

	}

	// Initialize values
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		packageTextBox.setText(store.getString(ICucumberPreferenceConstants.PREF_ADD_PACKAGE));
	}

	// Perform Apply/OK
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			storeValues();
			// System.out.println("performOk ...");
		}
		return result;
	}

	// Perform 'Restore Defaults'
	protected void performDefaults() {
		this.packageTextBox.setText("");
		super.performDefaults();
		// System.out.println("Restore Defaults ...");
	}

	// Store Values
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_ADD_PACKAGE, getText(this.packageTextBox));
	}

	// Get Text From TextBox
	private String getText(Text textBox) {
		packageNameText = textBox.getText().trim();
		return packageNameText.length() > 0 ? packageNameText : "";
	}

	// Get Image
	public static Image getImage(String imagePath) {
		LocalResourceManager manager = new LocalResourceManager(JFaceResources.getResources());
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, imagePath);
		Image image = manager.createImage(imageDescriptor);
		return image;
	}

	// Get Package Name(s) From User-Settings Page
	public String getPackageName() {
		IPreferenceStore store = getPreferenceStore();
		return store.getString(ICucumberPreferenceConstants.PREF_ADD_PACKAGE);
	}

	//#239:Only match step implementation in same package as feature file	
	public Boolean getOnlyPackages() {
		IPreferenceStore store = getPreferenceStore();
		return store.getBoolean(ICucumberPreferenceConstants.PREF_ONLY_SEARCH_PACKAGE);
	}
	
	public String getOnlySpecificPackage() {
		IPreferenceStore store = getPreferenceStore();
		return store.getString(ICucumberPreferenceConstants.PREF_ONLY_SEARCH_SPECIFIC_PACKAGE);
	}
	
	public static String getString(String key) {
		// TODO: load strings via .messages file from resource bundle...
		return key;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

}
