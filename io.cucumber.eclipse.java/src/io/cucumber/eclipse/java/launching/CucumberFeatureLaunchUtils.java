package io.cucumber.eclipse.java.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.ConsolePlugin;

import io.cucumber.eclipse.editor.EditorLogging;

public class CucumberFeatureLaunchUtils {

	private CucumberFeatureLaunchUtils() {
		// NO ENTRY NO INSTANCES
	}

	protected static IProject getProject() {
		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
				IFile file = input.getFile();
				IProject activeProject = file.getProject();
				return activeProject;
			}
		}
		return null;
	}

	public static boolean isAnsiConsoleEnabled() {
		ConsolePlugin.getDefault().getPreferenceStore().getBoolean("ANSI_support_enabled");
		return false;
	}

	protected static String getFeaturePath() {
		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
				return input.getFile().getLocation().toString();
			}
		}
		return null;
	}

	public static void updateFromConfig(ILaunchConfiguration config, String attrib, Text text) {
		String s = "";
		try {
			s = config.getAttribute(attrib, "");
		} catch (CoreException e) {
			EditorLogging.error("Failed to read launch configuration attribute: " + attrib, e);
		}
		text.setText(s);
	}
	
	public static boolean updateFromConfig(ILaunchConfiguration config, String attrib) {
		return getAttribute(config, attrib, false);
	}

	public static boolean getAttribute(ILaunchConfiguration config, String attrib, boolean defaultValue) {
		try {
			return config.getAttribute(attrib, defaultValue);
		} catch (CoreException e) {
		}
		return defaultValue;
	}
}
