package io.cucumber.eclipse.python.launching;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.osgi.framework.Bundle;

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
		
		// Get Python plugin path
		String pythonPluginPath = getPythonPluginPath();

		// Create message endpoint for receiving test results
		BehaveMessageEndpointProcess endpoint = null;
		try {
			endpoint = new BehaveMessageEndpointProcess(launch);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
				"Can't create remote process communication channel", e));
		}

		// Build and launch the behave process
		try {
			BehaveProcessLauncher launcher = new BehaveProcessLauncher()
				.withCommand(behaveCommand)
				.withFeaturePath(withLine != null && !withLine.isEmpty() ? withLine : featurePath)
				.withWorkingDirectory(workingDirectory)
				.withTags(tags)
				.withVerbose(isVerbose)
				.withNoCapture(isNoCapture)
				.withDryRun(isDryRun)
				.withRemoteConnection(endpoint.getPort(), pythonPluginPath);
			
			// Start the endpoint listener
			endpoint.start();
			launch.addProcess(endpoint);

			// Launch the behave process
			Process process = launcher.launch();
			IProcess iProcess = DebugPlugin.newProcess(launch, process, "Cucumber Behave");
			iProcess.setAttribute(IProcess.ATTR_PROCESS_TYPE, "cucumber.behave");
		} catch (IOException e) {
			if (endpoint != null) {
				endpoint.terminate();
			}
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
				"Failed to launch behave process", e));
		} catch (CoreException e) {
			if (endpoint != null) {
				endpoint.terminate();
			}
			throw e;
		} catch (RuntimeException e) {
			if (endpoint != null) {
				endpoint.terminate();
			}
			throw e;
		}
	}
	
	/**
	 * Get the path to the Python plugin directory
	 * 
	 * @return Path to python-plugins directory
	 * @throws CoreException if path cannot be determined
	 */
	private String getPythonPluginPath() throws CoreException {
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			URL pluginURL = FileLocator.find(bundle, new Path("python-plugins"), null);
			if (pluginURL != null) {
				URL fileURL = FileLocator.toFileURL(pluginURL);
				File pluginDir = new File(fileURL.toURI());
				if (pluginDir.exists()) {
					return pluginDir.getAbsolutePath();
				}
			}
			// Fallback: try to find in bundle location
			File bundleFile = FileLocator.getBundleFile(bundle);
			if (bundleFile != null) {
				File pluginDir = new File(bundleFile, "python-plugins");
				if (pluginDir.exists()) {
					return pluginDir.getAbsolutePath();
				}
			}
		} catch (IOException | URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
				"Failed to locate Python plugin directory", e));
		}
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
			"Python plugin directory not found"));
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
