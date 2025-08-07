package io.cucumber.eclipse.java.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.properties.CucumberJavaBackendProperties;

public final record CucumberJavaPreferences(IPreferenceStore store, IEclipsePreferences node, boolean showHooks,
		List<String> glueFilter,
		List<String> plugins) {

	static final String PREF_USE_STEP_DEFINITIONS_FILTERS = Activator.PLUGIN_ID + ".use_step_definitions_filters";
	static final String PREF_ACTIVE_FILTERS_LIST = Activator.PLUGIN_ID + ".active_filters";
	static final String PREF_INACTIVE_FILTERS_LIST = Activator.PLUGIN_ID + ".inactive_filters";
	static final String PREF_SHOW_HOOK_ANNOTATIONS = Activator.PLUGIN_ID + ".show_hooks";

	public static CucumberJavaPreferences of() {
		return of(null);
	}

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
	 * Parses the comma separated string into an array of strings
	 *
	 * @param listString a string representation of a list of elements separated by
	 *                   commas
	 * 
	 * @return an array of string
	 */
	static List<String> parseList(String listString) {
		if (listString == null || listString.isBlank()) {
			return List.of();
		}
		List<String> list = new ArrayList<String>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}

	/**
	 * Serializes the array of strings into one comma separated string.
	 *
	 * @param list array of strings
	 * @return a single string composed of the given list
	 */
	static String serializeList(String[] list) {
		if (list == null) {
			return ""; //$NON-NLS-1$
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

	public static void setShowHooks(boolean showhooks) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setShowHooks(store, showhooks);
	}


	protected static void setShowHooks(IPreferenceStore store, boolean showhooks) {
		store.setValue(CucumberJavaPreferences.PREF_SHOW_HOOK_ANNOTATIONS, showhooks);
	}
}
