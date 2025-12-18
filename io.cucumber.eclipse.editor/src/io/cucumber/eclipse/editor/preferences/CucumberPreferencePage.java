package io.cucumber.eclipse.editor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.launching.Mode;

public class CucumberPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {


	public CucumberPreferencePage() {
		super(FLAT);
		CucumberEditorPreferences editorPreferences = CucumberEditorPreferences.of();
		setPreferenceStore(editorPreferences.store());
	}
	
	@Override
	protected void createFieldEditors() {
				
		Composite parent = getFieldEditorParent();
		
		for (Mode mode : Mode.values()) {
			addField(new BooleanFieldEditor(
					CucumberEditorPreferences.PREF_SHOW_RUN_SHORTCUT_PREFIX + mode.name(),
					getLabelForMode(mode), parent));

		}
	}

	public static String getLabelForMode(Mode mode) {
		return getString(String.format("Show %s shortcut in feature files", mode));
	}

	public static Image getImage(String imagePath) {
		LocalResourceManager manager = new LocalResourceManager(JFaceResources.getResources());
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, imagePath);
		Image image = manager.createImage(imageDescriptor);
		return image;
	}

	public static String getString(String key) {
		// TODO: load strings via .messages file from resource bundle...
		return key;
	}

	@Override
	public void init(IWorkbench arg0) {
		// nothing to init here...
	}

}
