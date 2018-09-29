package cucumber.eclipse.editor.preferences;

import cucumber.eclipse.editor.Activator;

public interface ICucumberPreferenceConstants {

	public static final String _PREFIX = Activator.PLUGIN_ID + "."; //$NON-NLS-1$

	// Gherkin Formatting
	public static final String PREF_FORMAT_RIGHT_ALIGN_NUMERIC_VALUES_IN_TABLES =
			_PREFIX + "format_right_align_numeric_values_in_tables"; //$NON-NLS-1$
	public static final String PREF_FORMAT_CENTER_STEPS =
			_PREFIX + "format_center_steps"; //$NON-NLS-1$
	public static final String PREF_FORMAT_PRESERVE_BLANK_LINE_BETWEEN_STEPS =
			_PREFIX + "format_preserve_blank_line_between_steps"; //$NON-NLS-1$
	public static final String PREF_INDENTATION_STYLE =
			_PREFIX + "indentation_style"; //$NON-NLS-1$
	
	// Plugin Settings
	public static final String PREF_CHECK_STEP_DEFINITIONS =
			_PREFIX + "check_step_definitions"; //$NON-NLS-1$	
	//#239:Only match step implementation in same package as feature file
	public static final String PREF_ONLY_SEARCH_PACKAGE =
			_PREFIX + "only_search_package"; //$NON-NLS-1$	
	public static final String PREF_ONLY_SEARCH_SPECIFIC_PACKAGE = 
			_PREFIX + "only_search_specific_package"; //$NON-NLS-1$
	
	// Newly Declared By Girija for User-Settings Cucumber Preference Page
	public static final String PREF_ADD_PACKAGE =
    		_PREFIX + "add_package"; //$NON-NLS-1$
	
	public static enum CucumberIndentationStyle {
		TWO_SPACES("2 Spaces", "  "), 
		FOUR_SPACES("4 Spaces", "    "), 
		TABS("Tabs", "	");

		private final String label;
		private final String value;

		private CucumberIndentationStyle(String label, String value) {
			this.label = label;
			this.value = value;
		}

		public static String[][] getLabelsAndValues() {
			CucumberIndentationStyle[] values = values();
			String[][] labelsAndValues = new String[values.length][2];
			for (int i = 0; i < values.length; i++) {
				CucumberIndentationStyle value = values[i];
				labelsAndValues[i][0] = value.getLabel();
				labelsAndValues[i][1] = value.getValue();
			}
			return labelsAndValues;
		}

		public String getLabel() {
			return label;
		}

		public String getValue() {
			return value;
		}
	}
}
