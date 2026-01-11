package io.cucumber.eclipse.editor.preferences;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.launching.Mode;
import io.cucumber.eclipse.editor.properties.CucumberEditorProperties;

/**
 * Provides unified access to Cucumber editor preferences and properties.
 * <p>
 * This record combines workspace-level preferences with optional project-specific
 * overrides. When project-specific settings are enabled for a resource, those
 * values take precedence over workspace preferences.
 * </p>
 * <p>
 * <b>Usage:</b>
 * <pre>
 * // Workspace preferences only
 * CucumberEditorPreferences prefs = CucumberEditorPreferences.of();
 * 
 * // With project override support
 * CucumberEditorPreferences prefs = CucumberEditorPreferences.of(resource);
 * if (prefs.isShowShortcutFor(Mode.RUN)) {
 *     // display run shortcut...
 * }
 * </pre>
 * </p>
 * 
 * @param store the workspace preference store
 * @param node the project-specific preferences node, or null if using workspace preferences
 * 
 * @see CucumberEditorProperties for direct project property access
 */
public final record CucumberEditorPreferences(IPreferenceStore store, IEclipsePreferences node) {

	static final String PREF_SHOW_RUN_SHORTCUT_PREFIX = Activator.PLUGIN_ID + ".show_run_shortcut_";
	static final String PREF_VALIDATION_TIMEOUT = Activator.PLUGIN_ID + ".validation_timeout";
	public static final int DEFAULT_VALIDATION_TIMEOUT = 500;

	/**
	 * Creates a preferences instance using workspace settings only.
	 * 
	 * @return a preferences instance with workspace-level settings
	 */
	public static CucumberEditorPreferences of() {
		return of(null);
	}

	/**
	 * Creates a preferences instance with optional project override support.
	 * <p>
	 * If the resource's project has project-specific settings enabled, those
	 * values will be returned. Otherwise, workspace preferences are used.
	 * </p>
	 * 
	 * @param resource the resource whose project may override preferences, or null for workspace only
	 * @return a preferences instance with appropriate settings
	 */
	public static CucumberEditorPreferences of(IResource resource) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (resource != null) {
			CucumberEditorProperties properties = CucumberEditorProperties.of(resource);
			if (properties.isEnabled()) {
				// project settings overwrite preferences...
				return new CucumberEditorPreferences(store, properties.node());
			}
		}
		return new CucumberEditorPreferences(store, null);
	}

	/**
	 * Checks if the launch shortcut should be shown for the given mode.
	 * <p>
	 * If project-specific settings are active, returns the project value.
	 * Otherwise, returns the workspace preference.
	 * </p>
	 * 
	 * @param mode the launch mode (RUN, DEBUG, PROFILE)
	 * @return true if the shortcut should be shown, false otherwise
	 */
	public boolean isShowShortcutFor(Mode mode) {
		if (node != null) {
			// Use project-specific setting
			return node.getBoolean(CucumberEditorProperties.KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), true);
		}
		// Use workspace preference
		return store.getBoolean(PREF_SHOW_RUN_SHORTCUT_PREFIX + mode.name());
	}

	/**
	 * Sets the workspace preference for showing the launch shortcut for the given mode.
	 * 
	 * @param mode the launch mode (RUN, DEBUG, PROFILE)
	 * @param show true to show the shortcut, false to hide it
	 */
	public static void setShowShortcutFor(Mode mode, boolean show) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setShowShortcutFor(store, mode, show);
	}

	/**
	 * Sets the preference for showing the launch shortcut in the given preference store.
	 * 
	 * @param store the preference store to update
	 * @param mode the launch mode (RUN, DEBUG, PROFILE)
	 * @param show true to show the shortcut, false to hide it
	 */
	protected static void setShowShortcutFor(IPreferenceStore store, Mode mode, boolean show) {
		store.setValue(PREF_SHOW_RUN_SHORTCUT_PREFIX + mode.name(), show);
	}

	/**
	 * Gets the validation timeout in milliseconds.
	 * <p>
	 * This controls the delay between document changes and validation execution
	 * (debouncing). If project-specific settings are active, returns the project
	 * value. Otherwise, returns the workspace preference.
	 * </p>
	 * 
	 * @return the validation timeout in milliseconds
	 */
	public int getValidationTimeout() {
		if (node != null) {
			int timeout = node.getInt(CucumberEditorProperties.KEY_VALIDATION_TIMEOUT, DEFAULT_VALIDATION_TIMEOUT);
			return timeout > 0 ? timeout : DEFAULT_VALIDATION_TIMEOUT;
		}
		int timeout = store.getInt(PREF_VALIDATION_TIMEOUT);
		return timeout > 0 ? timeout : DEFAULT_VALIDATION_TIMEOUT;
	}

	/**
	 * Sets the workspace preference for validation timeout.
	 * 
	 * @param store the preference store to update
	 * @param timeout the validation timeout in milliseconds
	 */
	public static void setValidationTimeout(IPreferenceStore store, int timeout) {
		store.setValue(PREF_VALIDATION_TIMEOUT, timeout);
	}
}
