package io.cucumber.eclipse.editor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * Utility class for managing Eclipse activity enablement for Cucumber plugin features.
 * <p>
 * This class controls the visibility of various Cucumber-related UI elements through
 * Eclipse's activity framework. Activities can be enabled or disabled to show/hide
 * console output, launch configurations, property pages, and preference pages.
 * </p>
 */
public class CucumberActivitySupport {

	/** Activity ID for Cucumber console output */
	private static final String CONSOLE = "io.cucumber.eclipse.editor.console";
	
	/** Activity ID for Cucumber launch configurations */
	private static final String LAUNCH = "io.cucumber.eclipse.editor.launch";
	
	/** Activity ID for Cucumber property pages */
	private static final String PROPERTIES = "io.cucumber.eclipse.editor.properties";
	
	/** Activity ID for Cucumber preference pages */
	private static final String PREFERENCES = "io.cucumber.eclipse.editor.preferences";

	/**
	 * Enables or disables a specific Eclipse activity.
	 * 
	 * @param activityId the ID of the activity to enable or disable
	 * @param enabled {@code true} to enable the activity, {@code false} to disable it
	 */
	private static void setActivityEnabled(String activityId, boolean enabled) {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		Set<String> enabledActivities = new HashSet<>(activitySupport.getActivityManager().getEnabledActivityIds());

		if (enabled) {
			enabledActivities.add(activityId);
		} else {
			enabledActivities.remove(activityId);
		}
		activitySupport.setEnabledActivityIds(enabledActivities);
	}

	/**
	 * Disables all Cucumber-related activities.
	 * <p>
	 * This method disables preferences, properties, launch configurations, and console
	 * output activities. It is typically called when no Cucumber support is needed.
	 * </p>
	 */
	public static void disableAllCucumberActivities() {
		enablePreferences(false);
		enableProperties(false);
		enableLaunch(false);
		enableConsole(false);
	}

	/**
	 * Enables or disables the Cucumber console activity.
	 * <p>
	 * When enabled, Cucumber console output will be visible in the Eclipse console view.
	 * </p>
	 * 
	 * @param enabled {@code true} to enable console output, {@code false} to disable it
	 */
	public static void enableConsole(boolean enabled) {
		setActivityEnabled(CONSOLE, enabled);
	}

	/**
	 * Enables or disables the Cucumber launch configuration activity.
	 * <p>
	 * When enabled, Cucumber launch configurations will be available in the Eclipse
	 * launch configuration dialog and shortcuts.
	 * </p>
	 * 
	 * @param enabled {@code true} to enable launch configurations, {@code false} to disable them
	 */
	public static void enableLaunch(boolean enabled) {
		setActivityEnabled(LAUNCH, enabled);
	}

	/**
	 * Enables or disables the Cucumber property pages activity.
	 * <p>
	 * When enabled, Cucumber-related property pages will be visible in the project
	 * and resource properties dialogs.
	 * </p>
	 * 
	 * @param enabled {@code true} to enable property pages, {@code false} to disable them
	 */
	public static void enableProperties(boolean enabled) {
		setActivityEnabled(PROPERTIES, enabled);
	}

	/**
	 * Enables or disables the Cucumber preference pages activity.
	 * <p>
	 * When enabled, Cucumber-related preference pages will be visible in the Eclipse
	 * preferences dialog.
	 * </p>
	 * 
	 * @param enabled {@code true} to enable preference pages, {@code false} to disable them
	 */
	public static void enablePreferences(boolean enabled) {
		setActivityEnabled(PREFERENCES, enabled);
	}

}