package io.cucumber.eclipse.editor.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import io.cucumber.eclipse.editor.launching.Mode;

public record CucumberEditorProperties(IEclipsePreferences node) {

	private static final String NAMESPACE = "io.cucumber.eclipse.editor";
	private static final String KEY_ENABLE_PROJECT_SPECIFIC_SETTINGS = "enableProjectSpecific";
	private static final String KEY_SHOW_LAUNCH_SHORTCUT_PREFIX = "showShortcut";

	public static CucumberEditorProperties of(IResource resource) {
		if (resource == null) {
			return new CucumberEditorProperties(null);
		}
		IEclipsePreferences node = getNode(resource);
		return new CucumberEditorProperties(node);
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
		flush();
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

	static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(NAMESPACE);
		return node;
	}

	public boolean isShowShortcutFor(Mode mode) {
		if (node == null) {
			return true;
		}
		return node.getBoolean(KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), true);
	}

	public void setShowShortcutFor(Mode mode, boolean selection) {
		if (node == null) {
			return;
		}
		node.putBoolean(KEY_SHOW_LAUNCH_SHORTCUT_PREFIX + mode.name(), selection);
	}
}
