package io.cucumber.eclipse.python.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import io.cucumber.eclipse.python.Activator;

public class CucumberBehaveLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		
		// Get configuration attributes
		String featurePath = substituteVar(configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, ""));
		String withLine = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_WITH_LINE, "");
		String tags = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_TAGS, "");
		String workingDirectory = substituteVar(configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, ""));
		String pythonInterpreter = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_PYTHON_INTERPRETER, "python");
		boolean isVerbose = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_IS_VERBOSE, false);
		boolean isNoCapture = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_IS_NO_CAPTURE, false);
		boolean isDryRun = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_IS_DRY_RUN, false);

		// Validate feature path
		if (featurePath == null || featurePath.isEmpty()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Feature path is not specified"));
		}

		// Build behave command
		List<String> commandList = new ArrayList<>();
		commandList.add(pythonInterpreter);
		commandList.add("-m");
		commandList.add("behave");

		// Add feature path (with line number if specified)
		if (withLine != null && !withLine.isEmpty()) {
			commandList.add(withLine);
		} else {
			commandList.add(featurePath);
		}

		// Add tags if specified
		if (tags != null && !tags.isEmpty()) {
			commandList.add("--tags");
			commandList.add(tags);
		}

		// Add verbose flag
		if (isVerbose) {
			commandList.add("--verbose");
		}

		// Add no-capture flag
		if (isNoCapture) {
			commandList.add("--no-capture");
		}

		// Add dry-run flag
		if (isDryRun) {
			commandList.add("--dry-run");
		}

		// Set working directory
		File workingDir = null;
		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			workingDir = new File(workingDirectory);
			if (!workingDir.exists() || !workingDir.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					"Working directory does not exist: " + workingDirectory));
			}
		}

		// Convert command list to array
		String[] commandArray = commandList.toArray(new String[0]);

		// Create process
		ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
		if (workingDir != null) {
			processBuilder.directory(workingDir);
		}
		processBuilder.redirectErrorStream(true);

		try {
			Process process = processBuilder.start();
			IProcess iProcess = DebugPlugin.newProcess(launch, process, "Cucumber Behave");
			iProcess.setAttribute(IProcess.ATTR_PROCESS_TYPE, "cucumber.behave");
		} catch (Exception e) {
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
			System.out.println("Could not substitute variable " + s);
			return null;
		}
	}

}
