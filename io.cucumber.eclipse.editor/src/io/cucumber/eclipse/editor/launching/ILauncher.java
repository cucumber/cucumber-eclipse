package io.cucumber.eclipse.editor.launching;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.unittest.ui.ConfigureViewerSupport;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

/**
 * a launcher is capable of launching a cucumber run for a given resource and a
 * possible selection of elements
 * 
 * @author christoph
 *
 */
public interface ILauncher {


	public static final ConfigureViewerSupport TEST_RESULT_LISTENER_CONFIGURER = new ConfigureViewerSupport(
			"io.cucumber.eclipse.editor.testresults");

	enum Mode {
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

		public boolean showShortcut() {
			return Activator.getDefault().getPreferenceStore()
					.getBoolean(ICucumberPreferenceConstants.PREF_SHOW_RUN_SHORTCUT_PREFIX + name());
		}
	}

	default void launch(GherkinEditorDocument document, IStructuredSelection selection, Mode mode, boolean temporary,
			IProgressMonitor monitor) throws CoreException {
		launch(Collections.singletonMap(document, selection), mode, temporary, monitor);
	}

	/**
	 * performs a launch of the given document for the supplied selection and mode
	 * 
	 * @param selection the selection contains elements of the following types
	 *                  {@link Feature}s, {@link Scenario}s, {@link LaunchTag}s
	 * @param mode
	 * @param temporary TODO
	 * @param monitor
	 * @throws CoreException
	 */
	void launch(Map<GherkinEditorDocument, IStructuredSelection> selection, Mode mode,
			boolean temporary, IProgressMonitor monitor) throws CoreException;

	/**
	 * 
	 * @param resource the resource to check
	 * @return <code>true</code> if this launcher can launch cucumber resources for
	 *         the given resource
	 */
	boolean supports(IResource resource);

	/**
	 * @param mode
	 * @return <code>true</code> if the given mode is supported
	 */
	boolean supports(Mode mode);
}
