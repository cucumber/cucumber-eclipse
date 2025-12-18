package io.cucumber.eclipse.editor.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import io.cucumber.eclipse.editor.launching.Mode;

/**
 * Provides access to project-specific properties for the Cucumber editor.
 * <p>
 * This record encapsulates project-scoped settings stored in the Eclipse preferences
 * system. Properties are stored under the {@code io.cucumber.eclipse.editor} namespace
 * and can be configured via the project properties page.
 * </p>
 * <p>
 * <b>Usage:</b>
 * <pre>
 * CucumberEditorProperties props = CucumberEditorProperties.of(resource);
 * if (props.isEnabled()) {
 *     boolean showRun = props.isShowShortcutFor(Mode.RUN);
 * }
 * </pre>
 * </p>
 * 
 * @param node the Eclipse preferences node for project-specific settings, or null if not available
 * 
 * @see CucumberEditorPreferences for workspace-wide preferences with project override support
 */
public record CucumberEditorProperties(IEclipsePreferences node) {

	private static final String NAMESPACE = "io.cucumber.eclipse.editor";
	
	/** Key for enabling project-specific settings */
	static final String KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS = "enableProjectSpecific";
	
	/** Key prefix for launch shortcut visibility settings (suffixed with mode name) */
	public static final String KEY_SHOW_LAUNCH_SHORTCUT_PREFIX = "showShortcut";

	/**
	 * Creates a properties instance for the given resource.
	 * 
	 * @param resource the resource whose project properties should be accessed, or null
	 * @return a properties instance with the project's preference node, or with null node if resource is null
	 */
	public static CucumberEditorProperties of(IResource resource) {
		if (resource == null) {
			return new CucumberEditorProperties(null);
		}
		IEclipsePreferences node = getNode(resource);
		return new CucumberEditorProperties(node);
	}

	/**
	 * Checks if project-specific settings are enabled.
	 * 
	 * @return true if project-specific settings are enabled, false otherwise
	 */
	public boolean isEnabled() {
		if (node == null) {
			return false;
		}
		return node.getBoolean(KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, false);
	}

	/**
	 * Enables or disables project-specific settings.
	 * <p>
	 * When enabled, project properties override workspace preferences.
	 * This method automatically calls {@link #flush()} to persist the change.
	 * </p>
	 * 
	 * @param enabled true to enable project-specific settings, false to use workspace defaults
	 */
	public void setEnabled(boolean enabled) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, enabled);
		flush();
	}

	/**
	 * Persists all pending changes to the backing store.
	 * <p>
	 * This method should be called after making property changes to ensure
	 * they are saved. Exceptions during flush are silently ignored.
	 * </p>
	 */
	public void flush() {
		if (node == null) {
			return;
		}
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
	}

	/**
	 * Gets the Eclipse preferences node for the given resource's project.
	 * 
	 * @param resource the resource whose project node should be retrieved
	 * @return the preferences node for the project
	 */
	static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(NAMESPACE);
		return node;
	}

	/**
	 * Checks if the launch shortcut should be shown for the given mode.
	 * 
	 * @param mode the launch mode (RUN, DEBUG, PROFILE)
	 * @return true if the shortcut should be shown, false otherwise; defaults to true
	 */
	public boolean isShowShortcutFor(Mode mode) {
		if (node == null) {
			return true;
		}
		return node.getBoolean(KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), true);
	}

	/**
	 * Sets whether the launch shortcut should be shown for the given mode.
	 * <p>
	 * Call {@link #flush()} to persist the change.
	 * </p>
	 * 
	 * @param mode the launch mode (RUN, DEBUG, PROFILE)
	 * @param show true to show the shortcut, false to hide it
	 */
	public void setShowShortcutFor(Mode mode, boolean show) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), show);
	}
}
