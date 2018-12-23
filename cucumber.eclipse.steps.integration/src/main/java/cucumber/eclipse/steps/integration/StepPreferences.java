package cucumber.eclipse.steps.integration;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class StepPreferences {

	public static StepPreferences INSTANCE = new StepPreferences();

	// Plugin Settings
	public static String PREF_CHECK_STEP_DEFINITIONS = "check_step_definitions"; //$NON-NLS-1$
	// #239:Only match step implementation in same package as feature file
	public static String PREF_GLUE_ONLY_IN_SAME_LOCATION = "glue_only_in_same_location"; //$NON-NLS-1$
	// Newly Declared By Girija for User-Settings Cucumber Preference Page
	public static String PREF_ADD_PACKAGE = "add_package"; //$NON-NLS-1$

	private IPreferencesService preferencesService;
	
	private StepPreferences() {
		preferencesService = Platform.getPreferencesService();
	}

	public boolean isStepDefinitionsMatchingEnabled() {
		return preferencesService.getBoolean("cucumber.eclipse.editor", PREF_CHECK_STEP_DEFINITIONS, false, null);
	}
	
	// Get Package Name(s) From User-Settings Page
	public String getPackageName() {
		return preferencesService.getString("cucumber.eclipse.editor", PREF_ADD_PACKAGE, "", null);
	}

	// #239:Only match step implementation in same package as feature file
	public Boolean isGlueOnlyInSameLocationEnabled() {
		return preferencesService.getBoolean("cucumber.eclipse.editor", PREF_GLUE_ONLY_IN_SAME_LOCATION, false, null);
	}

}
