package cucumber.eclipse.steps.integration;

import org.eclipse.core.runtime.CoreException;

/**
 * The cucumber editor try to jump to step definition resource by itself when it
 * know the file and the line number. However in some case the step definition
 * is not into a file. For example, a step definition can be into a Java class
 * file from a JAR package.
 * 
 * In this case, the Cucumber Eclipse JDT plugin can define this own
 * <i>IStepDefinitionOpener</i> to allow the plugin to open this specific type
 * of resource.
 * 
 * @author qvdk
 *
 */
public interface IStepDefinitionOpener {

	/**
	 * This method indicates if the opener can opened the given step definition.
	 * 
	 * @param stepDefinition a step definition
	 * @return true if the
	 *         {@link IStepDefinitionOpener#openInEditor(StepDefinition)} can open
	 *         this step definition
	 */
	boolean canOpen(StepDefinition stepDefinition);

	/**
	 * Open an editor and focus on the step definition.
	 * 
	 * @param stepDefinition a step definition
	 * @throws CoreException if an error occurs
	 */
	void openInEditor(StepDefinition stepDefinition) throws CoreException;

}
