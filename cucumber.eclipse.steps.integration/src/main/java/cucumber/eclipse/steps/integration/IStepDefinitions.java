package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public interface IStepDefinitions {

	/**
	 * A StepDefinitions can be provided for any language: java, kotlin, groovy...
	 * The support method indicates if the current IStepDefinitions is allow to
	 * analyze the given stepDefinitionFile.
	 * 
	 * If this method return false, then the method getSteps must do nothing.
	 * 
	 * @param project a project where step definitions are located
	 * @return true if the IStepDefinition supports this type of file.
	 * @throws CoreException 
	 * 
	 */
	boolean support(IProject project) throws CoreException;
	
	void addStepListener(IStepListener listener);

	Set<Step> getSteps(IFile featureFile, IProgressMonitor progressMonitor) throws CoreException;

	void removeStepListener(IStepListener listener);

	void scan();
	
	void scan(IFile featureFile);
		
	/**
	 * @return a friendly name of the language parsed to detect step definitions
	 */
	String supportedLanguage();
	
	Set<Step> getLatestStepsDefinitionsScanResult();

	
	Set<Step> findStepDefintions(IFile stepDefinitionFile, MarkerFactory markerFactory, IProgressMonitor monitor) throws CoreException;
	
}
