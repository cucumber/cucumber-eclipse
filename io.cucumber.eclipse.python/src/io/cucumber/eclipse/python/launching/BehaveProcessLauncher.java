package io.cucumber.eclipse.python.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Builder for launching behave processes with various configurations.
 * Provides a fluent API to configure and launch behave.
 */
public class BehaveProcessLauncher {

	private String command = "behave";
	private String featurePath;
	private String workingDirectory;
	private List<String> additionalArgs = new ArrayList<>();
	
	/**
	 * Sets the behave command to use (defaults to "behave")
	 */
	public BehaveProcessLauncher withCommand(String command) {
		this.command = command;
		return this;
	}
	
	/**
	 * Sets the feature file or directory path
	 */
	public BehaveProcessLauncher withFeaturePath(String featurePath) {
		this.featurePath = featurePath;
		return this;
	}
	
	/**
	 * Sets the working directory for the process
	 */
	public BehaveProcessLauncher withWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
		return this;
	}
	
	/**
	 * Adds an additional command line argument
	 */
	public BehaveProcessLauncher withArgument(String arg) {
		this.additionalArgs.add(arg);
		return this;
	}
	
	/**
	 * Adds multiple command line arguments
	 */
	public BehaveProcessLauncher withArguments(List<String> args) {
		this.additionalArgs.addAll(args);
		return this;
	}
	
	/**
	 * Adds tag filter
	 */
	public BehaveProcessLauncher withTags(String tags) {
		if (tags != null && !tags.isEmpty()) {
			this.additionalArgs.add("--tags");
			this.additionalArgs.add(tags);
		}
		return this;
	}
	
	/**
	 * Enables verbose output
	 */
	public BehaveProcessLauncher withVerbose(boolean verbose) {
		if (verbose) {
			this.additionalArgs.add("--verbose");
		}
		return this;
	}
	
	/**
	 * Enables no-capture mode
	 */
	public BehaveProcessLauncher withNoCapture(boolean noCapture) {
		if (noCapture) {
			this.additionalArgs.add("--no-capture");
		}
		return this;
	}
	
	/**
	 * Enables dry-run mode
	 */
	public BehaveProcessLauncher withDryRun(boolean dryRun) {
		if (dryRun) {
			this.additionalArgs.add("--dry-run");
		}
		return this;
	}
	
	/**
	 * Sets the output format
	 */
	public BehaveProcessLauncher withFormat(String format) {
		if (format != null && !format.isEmpty()) {
			this.additionalArgs.add("--format");
			this.additionalArgs.add(format);
		}
		return this;
	}
	
	/**
	 * Disables summary output
	 */
	public BehaveProcessLauncher withNoSummary(boolean noSummary) {
		if (noSummary) {
			this.additionalArgs.add("--no-summary");
		}
		return this;
	}
	
	/**
	 * Launches the behave process with the configured parameters
	 * 
	 * @return the started Process
	 * @throws IOException if process creation fails
	 */
	public Process launch() throws IOException {
		List<String> commandList = new ArrayList<>();
		commandList.add(command);
		
		if (featurePath != null && !featurePath.isEmpty()) {
			commandList.add(featurePath);
		}
		
		commandList.addAll(additionalArgs);
		
		ProcessBuilder processBuilder = new ProcessBuilder(commandList);
		
		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			processBuilder.directory(new File(workingDirectory));
		}
		
		processBuilder.redirectErrorStream(true);
		
		return processBuilder.start();
	}
	
	/**
	 * Checks if a resource belongs to a Behave/Python project
	 * 
	 * @param resource the resource to check
	 * @return true if the resource is in a Python/Behave project
	 */
	public static boolean isBehaveProject(IResource resource) {
		if (resource == null) {
			return false;
		}
		
		// Check for Behave convention: .feature file with steps/ directory containing .py files
		if (resource.getType() == IResource.FILE && resource.getName().endsWith(".feature")) {
			// Look for a 'steps' subdirectory relative to the feature file
			org.eclipse.core.runtime.IPath featureParent = resource.getParent().getLocation();
			if (featureParent != null) {
				java.io.File stepsDir = new java.io.File(featureParent.toFile(), "steps");
				if (stepsDir.exists() && stepsDir.isDirectory()) {
					// Check if steps directory contains at least one .py file
					java.io.File[] files = stepsDir.listFiles();
					if (files != null) {
						for (java.io.File file : files) {
							if (file.isFile() && file.getName().endsWith(".py")) {
								return true;
							}
						}
					}
				}
			}
		}
		
		IProject project = resource.getProject();
		if (project == null) {
			return false;
		}
		
		// Check for PyDev nature
		try {
			if (project.hasNature("org.python.pydev.pythonNature")) {
				return true;
			}
		} catch (CoreException e) {
			// Ignore and try other checks
		}
		
		// Check for Python-related files/folders as fallback
		return project.getFile("requirements.txt").exists() ||
		       project.getFile("setup.py").exists() ||
		       project.getFile("pyproject.toml").exists() ||
		       project.getFolder("venv").exists() ||
		       project.getFolder(".venv").exists();
	}
}
