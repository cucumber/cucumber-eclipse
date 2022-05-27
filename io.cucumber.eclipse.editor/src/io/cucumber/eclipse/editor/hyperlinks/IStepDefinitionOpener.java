package io.cucumber.eclipse.editor.hyperlinks;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;

import io.cucumber.messages.types.Step;

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
	 * @param resource
	 * @return <code>true</code> if the opener can (possibly) open steps in the
	 *         given resource
	 * @throws CoreException
	 */
	boolean canOpen(IResource resource) throws CoreException;

	/**
	 * Open an editor and focus on the step definition.
	 * 
	 * @param textViewer
	 * @param resource
	 * @param step
	 * 
	 * @param stepDefinition a step definition
	 * @return <code>true</code> if the step was opened
	 * @throws CoreException if an error occurs
	 */
	boolean openInEditor(ITextViewer textViewer, IResource resource, Step step) throws CoreException;

}
