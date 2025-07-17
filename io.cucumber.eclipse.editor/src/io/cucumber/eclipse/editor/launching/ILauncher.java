package io.cucumber.eclipse.editor.launching;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.unittest.ui.ConfigureViewerSupport;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Scenario;

/**
 * a launcher is capable of launching a cucumber run for a given resource and a
 * possible selection of elements
 * 
 * @author christoph
 *
 */
public interface ILauncher {


	public static final ConfigureViewerSupport TEST_RESULT_LISTENER_CONFIGURER = new ConfigureViewerSupport(
			"io.cucumber.eclipse.editor.testresults");

	default void launch(GherkinEditorDocument document, IStructuredSelection selection, Mode mode, boolean temporary,
			IProgressMonitor monitor) throws CoreException {
		launch(Collections.singletonMap(document, selection), mode, temporary, monitor);
	}

	/**
	 * performs a launch of the given document for the supplied selection and mode
	 * 
	 * @param selection the selection contains elements of the following types
	 *                  {@link Feature}s, {@link Scenario}s, {@link LaunchTag}s
	 * @param mode
	 * @param temporary TODO
	 * @param monitor
	 * @throws CoreException
	 */
	void launch(Map<GherkinEditorDocument, IStructuredSelection> selection, Mode mode,
			boolean temporary, IProgressMonitor monitor) throws CoreException;

	/**
	 * 
	 * @param resource the resource to check
	 * @return <code>true</code> if this launcher can launch cucumber resources for
	 *         the given resource
	 */
	boolean supports(IResource resource);

	/**
	 * @param mode
	 * @return <code>true</code> if the given mode is supported
	 */
	boolean supports(Mode mode);
}
