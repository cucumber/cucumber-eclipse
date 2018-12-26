package cucumber.eclipse.steps.integration;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public abstract class AbstractStepDefinitionsProvider implements IStepDefinitionsProvider {

	@Override
	public Set<StepDefinition> findStepDefinitions(IResource stepDefinitionResource, MarkerFactory markerFactory,
			IProgressMonitor monitor) throws CoreException {
		IProject project = stepDefinitionResource.getProject();
		if(!this.support(project)) {
			return new HashSet<StepDefinition>(0);
		}
		return this.findStepDefinitionsFromSupportedResource(stepDefinitionResource, markerFactory, monitor);
	}

	@Override
	public abstract boolean support(IProject project) throws CoreException;
	
	protected abstract Set<StepDefinition> findStepDefinitionsFromSupportedResource(IResource stepDefinitionFile, MarkerFactory markerFactory,
			IProgressMonitor monitor) throws CoreException;

}
