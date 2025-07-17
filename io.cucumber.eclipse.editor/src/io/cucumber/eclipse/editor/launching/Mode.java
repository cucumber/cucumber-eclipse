package io.cucumber.eclipse.editor.launching;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants;
import io.cucumber.eclipse.editor.properties.CucumberEditorProperties;

public enum Mode {
	RUN, DEBUG, PROFILE;

	private String label;

	@Override
	public String toString() {
		if (label != null) {
			return label;
		}
		ILaunchMode mode = getLaunchMode();
		if (mode != null) {
			return label = mode.getLabel().replace("&", "");
		}
		return super.toString();
	}

	public ILaunchMode getLaunchMode() {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			switch (this) {
			case RUN:
				return manager.getLaunchMode(ILaunchManager.RUN_MODE);
			case DEBUG:
				return manager.getLaunchMode(ILaunchManager.DEBUG_MODE);
			case PROFILE:
				return manager.getLaunchMode(ILaunchManager.PROFILE_MODE);
			default:
				break;
			}
		} catch (RuntimeException e) {
			// can't determine it then...
		}
		return null;
	}

	public static Mode parseString(String mode) {
		if (ILaunchManager.RUN_MODE.equalsIgnoreCase(mode)) {
			return Mode.RUN;
		}
		if (ILaunchManager.DEBUG_MODE.equalsIgnoreCase(mode)) {
			return Mode.DEBUG;
		}
		if (ILaunchManager.PROFILE_MODE.equalsIgnoreCase(mode)) {
			return Mode.PROFILE;
		}
		return null;
	}

	public String getSymbol() {
		switch (this) {
		case RUN:
			return String.valueOf((char) 0x25B7);
		case DEBUG:
			// TODO this unicode symbol would be better but its currently not rendered
			// correctly return "\uD83D\uDC1B";

			// return String.valueOf((char) 0x1F41E);
			return String.valueOf((char) 0x25C9);
		case PROFILE:
			// TODO this unicode symbol would be better but its currently not rendered
			// correctly return "\uD83D\uDD0D";
			return String.valueOf((char) 0x2690);
		default:
			return "";
		}

	}

	public boolean showShortcut(IResource resource) {
		CucumberEditorProperties properties = CucumberEditorProperties.of(resource);
		if (properties.isEnabled()) {
			return properties.isShowShortcutFor(this);
		}
		return Activator.getDefault().getPreferenceStore()
				.getBoolean(ICucumberPreferenceConstants.PREF_SHOW_RUN_SHORTCUT_PREFIX + name());
	}
}