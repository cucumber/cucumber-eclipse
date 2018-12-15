package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public interface IStepDefinitionsProvider {
	
	/** Find step definitions from a file.
	 * 
	 * By convention this method should return 
	 * 
	 * @param stepDefinitionFile
	 * @param markerFactory
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	Set<StepDefinition> findStepDefinitions(IFile stepDefinitionFile, MarkerFactory markerFactory,
			IProgressMonitor monitor) throws CoreException;

	boolean support(IProject project) throws CoreException;
	
}
