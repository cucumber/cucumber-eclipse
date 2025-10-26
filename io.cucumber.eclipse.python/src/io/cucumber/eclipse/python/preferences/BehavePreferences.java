package io.cucumber.eclipse.python.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import io.cucumber.eclipse.python.Activator;

/**
 * Manages Behave preferences at workspace and project level
 */
public class BehavePreferences {
	
	public static final String PREF_BEHAVE_COMMAND = "behave.command";
	public static final String DEFAULT_BEHAVE_COMMAND = "behave";
	
	private final IPreferenceStore store;
	private final IEclipsePreferences node;
	
	private BehavePreferences(IPreferenceStore store, IEclipsePreferences node) {
		this.store = store;
		this.node = node;
	}
	
	/**
	 * Get workspace-level preferences
	 */
	public static BehavePreferences of() {
		return of((IResource) null);
	}
	
	/**
	 * Get project-level preferences for a resource
	 */
	public static BehavePreferences of(IResource resource) {
		if (resource != null) {
			IProject project = resource.getProject();
			if (project != null) {
				return of(project);
			}
		}
		// Return workspace-level preferences
		ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		return new BehavePreferences(store, node);
	}
	
	/**
	 * Get project-level preferences
	 */
	public static BehavePreferences of(IProject project) {
		if (project != null) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(new ProjectScope(project), Activator.PLUGIN_ID);
			IEclipsePreferences node = new ProjectScope(project).getNode(Activator.PLUGIN_ID);
			return new BehavePreferences(store, node);
		}
		return of((IResource) null);
	}
	
	/**
	 * Get the behave command to use
	 */
	public String behaveCommand() {
		if (store != null) {
			String command = store.getString(PREF_BEHAVE_COMMAND);
			if (command != null && !command.isEmpty()) {
				return command;
			}
		}
		return DEFAULT_BEHAVE_COMMAND;
	}
	
	/**
	 * Get the preference store
	 */
	public IPreferenceStore store() {
		return store;
	}
	
	/**
	 * Get the Eclipse preferences node
	 */
	public IEclipsePreferences node() {
		return node;
	}
}
