package cucumber.eclipse.editor.preferences;

import cucumber.eclipse.editor.Activator;

public interface ICucumberPreferenceConstants {

	public static final String _PREFIX = Activator.PLUGIN_ID + "."; //$NON-NLS-1$

	// Preference constants
	public static final String PREF_FORMAT_RIGHT_ALIGN_NUMERIC_VALUES_IN_TABLES =
			_PREFIX + "format_right_align_numeric_values_in_tables"; //$NON-NLS-1$
	public static final String PREF_FORMAT_CENTER_STEPS =
			_PREFIX + "format_center_steps"; //$NON-NLS-1$
	public static final String PREF_FORMAT_PRESERVE_BLANK_LINE_BETWEEN_STEPS =
			_PREFIX + "format_preserve_blank_line_between_steps"; //$NON-NLS-1$
	public static final String PREF_CHECK_STEP_DEFINITIONS =
			_PREFIX + "check_step_definitions"; //$NON-NLS-1$
}
