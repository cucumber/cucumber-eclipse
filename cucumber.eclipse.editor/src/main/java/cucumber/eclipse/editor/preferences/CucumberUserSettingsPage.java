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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import cucumber.eclipse.editor.Activator;

/**
 * @author girija.panda@nokia.com
 * 
 * Purpose: Creating a New 'User Settings' Preference Page as Cucumber-Preference. 
 * 			This 'User Settings' Preference allows user to add/input The Package Name of External Class-Path JAR having step definitions
 * 			And All steps can be reused/Populated into the current project
 * Cucumber Preference Page : Cucumber User Settings
 * Input Field Name : Add Root Package Name Of JAR File
 * Input Field Value : <Any Root Package Name> exists in class-path JAR, ex. com.nokia.bdd
 * 
 */

public class CucumberUserSettingsPage extends PreferencePage 
	   implements IWorkbenchPreferencePage {

	private String description = "Cucumber User Settings";
	private String cucumberImage = "icons/cukes.gif";
	
	private Label PackageLabel = null;
	private String packageLabelName = "Add Root Package Name Of Your Class-Path Dependency(JAR/POM...etc)";
	private String packageToolTip = "Add Root Package Name Of Your Class-Path Dependency(JAR/POM-Dependency...etc) Contains all Step-Definitions";

	private Text packageTextBox = null;
	public static String packageNameText = null;

	
	public CucumberUserSettingsPage() {
		//setTitle(description);
		//setDescription(description);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, 0);
		composite.setLayout(new GridLayout(2, false));

		//Description and Image
		CLabel label = new CLabel(composite, SWT.NULL);
		label.setText(getString(description));
		label.setImage(getImage(cucumberImage));
		
		// Package Label
		this.PackageLabel = new Label(composite, 0);
		this.PackageLabel.setLayoutData(new GridData(4, 16777216, true, false, 2, 1));
		this.PackageLabel.setText(packageLabelName);
		this.PackageLabel.setToolTipText(packageToolTip);

		// Package Text Box
		this.packageTextBox = new Text(composite, 2048);
		this.packageTextBox.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
		
		this.packageTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				CucumberUserSettingsPage.packageNameText = CucumberUserSettingsPage.this.packageTextBox.getText();
			}
		});

		//Initialize values
		initializeValues();

		return composite;
	}

	
	//Initialize values
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		packageTextBox.setText(store.getString(ICucumberPreferenceConstants.PREF_ADD_PACKAGE));
	}

	
	// Perform Apply/OK
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			storeValues();
			//System.out.println("performOk ...");
		}
		return result;
	}

	
	// Perform 'Restore Defaults'
	protected void performDefaults()
	  {
	    this.packageTextBox.setText("");
	    super.performDefaults();
	    //System.out.println("Restore Defaults ...");
	  }
	
	// Store Values
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_ADD_PACKAGE, getText(this.packageTextBox));
		//System.out.println("Text = " + packageNameText);
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

	
	
	// Get Package Name
	public String getPackageName() {
		IPreferenceStore store = getPreferenceStore();
		String myPackage = store.getString(ICucumberPreferenceConstants.PREF_ADD_PACKAGE);
		//System.out.println("My Package = " + myPackage);
		return myPackage;		
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
