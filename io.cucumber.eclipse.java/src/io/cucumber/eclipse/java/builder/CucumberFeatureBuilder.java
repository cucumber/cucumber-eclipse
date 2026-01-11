package io.cucumber.eclipse.java.builder;

import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;

import io.cucumber.eclipse.java.validation.JavaGlueValidator;

/**
 * Builder for validating Cucumber feature files in a project.
 * <p>
 * This builder processes all .feature files in the project and triggers
 * validation to update markers for unmatched steps and other glue code issues.
 * Since glue code (Java files, class files) can change and affect validation,
 * this builder always performs a full build rather than incremental builds.
 * </p>
 * 
 * @author cucumber-eclipse
 */
public class CucumberFeatureBuilder extends IncrementalProjectBuilder {

	/**
	 * ID of the Cucumber Feature Builder
	 */
	public static final String BUILDER_ID = "io.cucumber.eclipse.java.cucumberFeatureBuilder";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		try {
			JavaGlueValidator.validateProject(getProject(), monitor);
		} catch (Exception e) {
			ILog.get().error("Failed to validate project: " + getProject().getName(), e);
		}
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// Clean is handled by marker deletion which is automatic when resources are cleaned
	}

	/**
	 * Adds the Cucumber builder to the given project.
	 * 
	 * @param project the project to add the builder to
	 * @throws CoreException if the builder could not be added
	 */
	public static void addBuilder(IProject project) throws CoreException {
		if (!project.isOpen()) {
			return;
		}
		
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		// Check if builder is already present
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(BUILDER_ID)) {
				return; // Already configured
			}
		}

		// Add builder to project
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	/**
	 * Removes the Cucumber builder from the given project.
	 * 
	 * @param project the project to remove the builder from
	 * @throws CoreException if the builder could not be removed
	 */
	public static void removeBuilder(IProject project) throws CoreException {
		if (!project.isOpen()) {
			return;
		}
		
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				return;
			}
		}
	}

	/**
	 * Checks if the given project has the Cucumber builder.
	 * 
	 * @param project the project to check
	 * @return true if the project has the builder, false otherwise
	 */
	public static boolean hasBuilder(IProject project) {
		if (!project.isOpen()) {
			return false;
		}
		try {
			IProjectDescription description = project.getDescription();
			ICommand[] commands = description.getBuildSpec();
			for (int i = 0; i < commands.length; ++i) {
				if (commands[i].getBuilderName().equals(BUILDER_ID)) {
					return true;
				}
			}
		} catch (CoreException e) {
			// Ignore
		}
		return false;
	}
}
