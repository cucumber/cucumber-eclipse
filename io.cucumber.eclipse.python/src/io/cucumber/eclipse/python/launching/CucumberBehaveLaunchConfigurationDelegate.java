package io.cucumber.eclipse.python.launching;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.eclipse.python.preferences.BehavePreferences;

public class CucumberBehaveLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		
		// Get configuration attributes
		String featurePath = substituteVar(configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, ""));
		String withLine = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_WITH_LINE, "");
		String tags = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_TAGS, "");
		String workingDirectory = substituteVar(configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, ""));
		boolean isVerbose = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_IS_VERBOSE, false);
		boolean isNoCapture = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_IS_NO_CAPTURE, false);
		boolean isDryRun = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_IS_DRY_RUN, false);

		// Validate feature path
		if (featurePath == null || featurePath.isEmpty()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Feature path is not specified"));
		}

		// Validate working directory
		File workingDir = null;
		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			workingDir = new File(workingDirectory);
			if (!workingDir.exists() || !workingDir.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					"Working directory does not exist: " + workingDirectory));
			}
		}

		// Get behave command from preferences
		BehavePreferences preferences = BehavePreferences.of();
		String behaveCommand = preferences.behaveCommand();

		// Build and launch the behave process
		try {
			BehaveProcessLauncher launcher = new BehaveProcessLauncher()
				.withCommand(behaveCommand)
				.withFeaturePath(withLine != null && !withLine.isEmpty() ? withLine : featurePath)
				.withWorkingDirectory(workingDirectory)
				.withTags(tags)
				.withVerbose(isVerbose)
				.withNoCapture(isNoCapture)
				.withDryRun(isDryRun);

			Process process = launcher.launch();
			IProcess iProcess = DebugPlugin.newProcess(launch, process, "Cucumber Behave");
			iProcess.setAttribute(IProcess.ATTR_PROCESS_TYPE, "cucumber.behave");
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
				"Failed to launch behave process", e));
		}
	}

	/**
	 * Substitute any variable
	 */
	private static String substituteVar(String s) {
		if (s == null) {
			return s;
		}
		try {
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(s);
		} catch (CoreException e) {
			EditorLogging.error("Could not substitute variable: " + s, e);
			return null;
		}
	}

}
