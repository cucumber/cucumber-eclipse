package io.cucumber.eclipse.editor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.launching.Mode;

public class CucumberPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		for (Mode mode : Mode.values()) {
			store.setDefault(CucumberEditorPreferences.PREF_SHOW_RUN_SHORTCUT_PREFIX + mode.name(), true);
		}
	}

}
