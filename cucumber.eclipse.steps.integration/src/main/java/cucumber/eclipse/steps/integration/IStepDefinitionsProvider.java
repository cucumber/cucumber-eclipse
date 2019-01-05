package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public interface IStepDefinitionsProvider {

	/**
	 * Find step definitions into a file.
	 * 
	 * @param stepDefinitionResource the file where to search step definition.
	 * @param markerFactory      factory of markers supported by this plugin
	 * @param monitor            the progress monitor
	 * @return a set of StepDefinition or an empty array. Should NEVER return null.
	 * @throws CoreException if an error occurs
	 */
	Set<StepDefinition> findStepDefinitions(IResource stepDefinitionResource, MarkerFactory markerFactory,
			IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Indicate if this step definitions provider support the current kind of
	 * project. For example, the JDT step definition provider supports only Java
	 * project.
	 * 
	 * @param project a project
	 * @return true when the step definitions provider is able to search step
	 *         definitions in this project
	 * @throws CoreException if an error occurs
	 */
	boolean support(IProject project) throws CoreException;

	/**
	 * Indicate if the step definition provider is able to 
	 * parse the resource to scan step definitions. 
	 * @param resource a resource potentially a step definitions source
	 * @return true if the file could be analyzed
	 * @throws CoreException if an error occurs
	 */
	boolean support(IResource resource) throws CoreException;

}
