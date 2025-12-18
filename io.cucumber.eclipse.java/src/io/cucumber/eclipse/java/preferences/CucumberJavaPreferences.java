package io.cucumber.eclipse.java.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.properties.CucumberJavaBackendProperties;

/**
 * Provides unified access to Cucumber Java backend preferences and properties.
 * <p>
 * This record combines workspace-level preferences with optional project-specific
 * overrides. When project-specific settings are enabled for a resource, those
 * values take precedence over workspace preferences.
 * </p>
 * <p>
 * <b>Usage:</b>
 * <pre>
 * // Workspace preferences only
 * CucumberJavaPreferences prefs = CucumberJavaPreferences.of();
 * 
 * // With project override support
 * CucumberJavaPreferences prefs = CucumberJavaPreferences.of(resource);
 * if (prefs.showHooks()) {
 *     // display hooks...
 * }
 * </pre>
 * </p>
 * 
 * @param store the workspace preference store
 * @param node the project-specific preferences node, or null if using workspace preferences
 * @param showHooks whether to show hook annotations in feature files
 * @param glueFilter list of active glue code package filters
 * @param plugins list of validation plugin class names
 * 
 * @see CucumberJavaBackendProperties for direct project property access
 */
public final record CucumberJavaPreferences(IPreferenceStore store, IEclipsePreferences node, boolean showHooks,
		List<String> glueFilter,
		List<String> plugins) {

	static final String PREF_USE_STEP_DEFINITIONS_FILTERS = Activator.PLUGIN_ID + ".use_step_definitions_filters";
	static final String PREF_ACTIVE_FILTERS_LIST = Activator.PLUGIN_ID + ".active_filters";
	static final String PREF_INACTIVE_FILTERS_LIST = Activator.PLUGIN_ID + ".inactive_filters";
	static final String PREF_SHOW_HOOK_ANNOTATIONS = Activator.PLUGIN_ID + ".show_hooks";

	/**
	 * Creates a preferences instance using workspace settings only.
	 * 
	 * @return a preferences instance with workspace-level settings
	 */
	public static CucumberJavaPreferences of() {
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
	public static CucumberJavaPreferences of(IResource resource) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (resource != null) {
			CucumberJavaBackendProperties properties = CucumberJavaBackendProperties.of(resource);
			if (properties.isEnabled()) {
				// project settings overwrite preferences...
				return new CucumberJavaPreferences(store, properties.node(), properties.isShowHooks(),
						properties.getGlueFilter().toList(), properties.getPlugins().toList());
			}
		}
		boolean showHooks = store.getBoolean(PREF_SHOW_HOOK_ANNOTATIONS);
		String string = store.getString(CucumberJavaPreferences.PREF_ACTIVE_FILTERS_LIST);
		return new CucumberJavaPreferences(store, null, showHooks, parseList(string), List.of());
	}

	/**
	 * Parses a comma-separated string into a list of strings.
	 *
	 * @param listString a string representation of a list of elements separated by commas
	 * @return a list of strings, empty if the input is null or blank
	 */
	static List<String> parseList(String listString) {
		if (listString == null || listString.isBlank()) {
			return List.of();
		}
		List<String> list = new ArrayList<String>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ",");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}

	/**
	 * Serializes an array of strings into a comma-separated string.
	 *
	 * @param list array of strings to serialize
	 * @return a comma-separated string, empty string if the array is null
	 */
	static String serializeList(String[] list) {
		if (list == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append(list[i]);
		}
		return buffer.toString();
	}

	/**
	 * Sets the workspace preference for showing hook annotations.
	 * 
	 * @param showHooks true to show hook annotations, false to hide them
	 */
	public static void setShowHooks(boolean showHooks) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setShowHooks(store, showHooks);
	}

	/**
	 * Sets the preference for showing hook annotations in the given preference store.
	 * 
	 * @param store the preference store to update
	 * @param showHooks true to show hook annotations, false to hide them
	 */
	protected static void setShowHooks(IPreferenceStore store, boolean showHooks) {
		store.setValue(CucumberJavaPreferences.PREF_SHOW_HOOK_ANNOTATIONS, showHooks);
	}
}
