package io.cucumber.eclipse.java.properties;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public final record CucumberJavaBackendProperties(IEclipsePreferences node) {

	static final String NAMESPACE = "io.cucumber.eclipse.java";
	static final String KEY_INACTIVE_FILTER = "inactiveFilters";
	static final String KEY_ACTIVE_FILTER = "activeFilters";
	static final String KEY_VALIDATION_PLUGINS = "validationPlugins";
	static final String KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS = "enableProjectSpecific";
	static final String KEY_SHOW_HOOK = "enableShowHook";


	public static CucumberJavaBackendProperties of(IResource resource) {
		if (resource == null) {
			return new CucumberJavaBackendProperties(null);
		}
		IEclipsePreferences node = getNode(resource);
		return new CucumberJavaBackendProperties(node);
	}

	public boolean isEnabled() {
		if (node == null) {
			return false;
		}
		return node.getBoolean(KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, false);
	}

	public void setEnabled(boolean enabled) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS, enabled);
	}

	public void flush() {
		if (node == null) {
			return;
		}
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
	}

	public Stream<String> getPlugins() {
		if (node == null) {
			return Stream.empty();
		}
		return parseList(node.get(CucumberJavaBackendProperties.KEY_VALIDATION_PLUGINS, ""));
	}

	public void setPlugins(Stream<String> plugins) {
		if (node == null) {
			return;
		}
		node.put(CucumberJavaBackendProperties.KEY_VALIDATION_PLUGINS, plugins.collect(Collectors.joining(",")));
	}

	public Stream<String> getGlueFilter() {
		if (node == null) {
			return Stream.empty();
		}
		return parseList(node.get(CucumberJavaBackendProperties.KEY_ACTIVE_FILTER, ""));
	}

	public void setGlueFilter(Stream<String> plugins) {
		if (node == null) {
			return;
		}
		node.put(CucumberJavaBackendProperties.KEY_ACTIVE_FILTER, plugins.collect(Collectors.joining(",")));
	}

	public boolean isShowHooks() {
		if (node == null) {
			return false;
		}
		return node.getBoolean(KEY_SHOW_HOOK, false);
	}

	public void setShowHooks(boolean show) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_SHOW_HOOK, show);
	}

	static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(CucumberJavaBackendProperties.NAMESPACE);
		return node;
	}

	static Stream<String> parseList(String string) {
		return Arrays.stream(string.split(",")).map(String::trim).filter(Predicate.not(String::isBlank));

	}
}