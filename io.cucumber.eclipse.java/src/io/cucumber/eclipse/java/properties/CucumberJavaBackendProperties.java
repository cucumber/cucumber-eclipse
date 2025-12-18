package io.cucumber.eclipse.java.properties;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Provides access to project-specific properties for the Cucumber Java backend.
 * <p>
 * This record encapsulates project-scoped settings stored in the Eclipse preferences
 * system. Properties are stored under the {@code io.cucumber.eclipse.java} namespace
 * and can be configured via the project properties page.
 * </p>
 * <p>
 * <b>Usage:</b>
 * <pre>
 * CucumberJavaBackendProperties props = CucumberJavaBackendProperties.of(resource);
 * if (props.isEnabled()) {
 *     Stream&lt;String&gt; glueFilters = props.getGlueFilter();
 * }
 * </pre>
 * </p>
 * 
 * @param node the Eclipse preferences node for project-specific settings, or null if not available
 * 
 * @see CucumberJavaPreferences for workspace-wide preferences with project override support
 */
public final record CucumberJavaBackendProperties(IEclipsePreferences node) {

	static final String NAMESPACE = "io.cucumber.eclipse.java";
	static final String KEY_INACTIVE_FILTER = "inactiveFilters";
	static final String KEY_ACTIVE_FILTER = "activeFilters";
	static final String KEY_VALIDATION_PLUGINS = "validationPlugins";
	static final String KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS = "enableProjectSpecific";
	static final String KEY_SHOW_HOOK = "enableShowHook";

	/**
	 * Creates a properties instance for the given resource.
	 * 
	 * @param resource the resource whose project properties should be accessed, or null
	 * @return a properties instance with the project's preference node, or with null node if resource is null
	 */
	public static CucumberJavaBackendProperties of(IResource resource) {
		if (resource == null) {
			return new CucumberJavaBackendProperties(null);
		}
		IEclipsePreferences node = getNode(resource);
		return new CucumberJavaBackendProperties(node);
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
	 * Call {@link #flush()} to persist the change.
	 * </p>
	 * 
	 * @param enabled true to enable project-specific settings, false to use workspace defaults
	 */
	public void setEnabled(boolean enabled) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, enabled);
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
	 * Gets the validation plugins configured for this project.
	 * <p>
	 * Validation plugins are used during glue code validation regardless of
	 * feature file settings. The returned stream contains fully qualified class names.
	 * </p>
	 * 
	 * @return a stream of plugin class names, empty if none configured or node unavailable
	 */
	public Stream<String> getPlugins() {
		if (node == null) {
			return Stream.empty();
		}
		return parseList(node.get(CucumberJavaBackendProperties.KEY_VALIDATION_PLUGINS, ""));
	}

	/**
	 * Sets the validation plugins for this project.
	 * <p>
	 * Call {@link #flush()} to persist the change.
	 * </p>
	 * 
	 * @param plugins a stream of fully qualified plugin class names
	 */
	public void setPlugins(Stream<String> plugins) {
		if (node == null) {
			return;
		}
		node.put(CucumberJavaBackendProperties.KEY_VALIDATION_PLUGINS, plugins.collect(Collectors.joining(",")));
	}

	/**
	 * Gets the active glue code package filters.
	 * <p>
	 * Glue filters restrict which packages are scanned for step definitions,
	 * improving performance and reducing false matches.
	 * </p>
	 * 
	 * @return a stream of package filter patterns, empty if none configured or node unavailable
	 */
	public Stream<String> getGlueFilter() {
		if (node == null) {
			return Stream.empty();
		}
		return parseList(node.get(CucumberJavaBackendProperties.KEY_ACTIVE_FILTER, ""));
	}

	/**
	 * Sets the active glue code package filters.
	 * <p>
	 * Call {@link #flush()} to persist the change.
	 * </p>
	 * 
	 * @param filters a stream of package filter patterns
	 */
	public void setGlueFilter(Stream<String> filters) {
		if (node == null) {
			return;
		}
		node.put(CucumberJavaBackendProperties.KEY_ACTIVE_FILTER, filters.collect(Collectors.joining(",")));
	}

	/**
	 * Checks if hook annotations should be displayed in feature files.
	 * 
	 * @return true if hook annotations should be shown, false otherwise
	 */
	public boolean isShowHooks() {
		if (node == null) {
			return false;
		}
		return node.getBoolean(KEY_SHOW_HOOK, false);
	}

	/**
	 * Sets whether hook annotations should be displayed in feature files.
	 * <p>
	 * Call {@link #flush()} to persist the change.
	 * </p>
	 * 
	 * @param show true to show hook annotations, false to hide them
	 */
	public void setShowHooks(boolean show) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_SHOW_HOOK, show);
	}

	/**
	 * Gets the Eclipse preferences node for the given resource's project.
	 * 
	 * @param resource the resource whose project node should be retrieved
	 * @return the preferences node for the project
	 */
	static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(CucumberJavaBackendProperties.NAMESPACE);
		return node;
	}

	/**
	 * Parses a comma-separated list into a stream of non-blank strings.
	 * 
	 * @param string the comma-separated string to parse
	 * @return a stream of trimmed, non-blank strings
	 */
	static Stream<String> parseList(String string) {
		return Arrays.stream(string.split(",")).map(String::trim).filter(Predicate.not(String::isBlank));
	}
}