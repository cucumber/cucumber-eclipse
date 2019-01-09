package cucumber.eclipse.steps.jdt.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;

import cucumber.eclipse.steps.jdt.Activator;

public abstract class CucumberJavaPreferences {

	public static final String PREF_USE_STEP_DEFINITIONS_FILTERS = Activator.PLUGIN_ID + ".use_step_definitions_filters";
	public static final String PREF_ACTIVE_FILTERS_LIST = Activator.PLUGIN_ID + ".active_filters";
	public static final String PREF_INACTIVE_FILTERS_LIST = Activator.PLUGIN_ID + ".inactive_filters";

	public static boolean isUseStepDefinitionsFilters() {
		return Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, PREF_USE_STEP_DEFINITIONS_FILTERS,
				false, null);
	}

	public static String[] getStepDefinitionsFilters() {
		return parseList(Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PREF_ACTIVE_FILTERS_LIST, "", null));
	}

	/**
	 * Parses the comma separated string into an array of strings
	 *
	 * @param listString a string representation of a list of elements separated by commas 
	 * 
	 * @return an array of string
	 */
	public static String[] parseList(String listString) {
		List<String> list = new ArrayList<String>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Serializes the array of strings into one comma separated string.
	 *
	 * @param list array of strings
	 * @return a single string composed of the given list
	 */
	public static String serializeList(String[] list) {
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

}
