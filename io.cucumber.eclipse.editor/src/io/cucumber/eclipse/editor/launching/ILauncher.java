package io.cucumber.eclipse.editor.launching;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

/**
 * a launcher is capable of launching a cucumber run for a given resource and a
 * possible selection of elements
 * 
 * @author christoph
 *
 */
public interface ILauncher {

	enum Mode {
		RUN, DEBUG;

		@Override
		public String toString() {
			switch (this) {
			case RUN:
				return "Run";
			case DEBUG:
				return "Debug";
			default:
				break;
			}
			return super.toString();
		}
	}

	default Stream<Envelope> launch(GherkinEditorDocument document, IStructuredSelection selection, Mode mode) {
		return launch(Collections.singletonMap(document, selection), mode);
	}

	/**
	 * performs a launch of the given document for the supplied selection and mode
	 * 
	 * @param selection the selection contains elements of the following types
	 *                  {@link Feature}s, {@link Scenario}s, {@link LaunchTag}s
	 * @param mode
	 * @return a stream of messages for the given run
	 */
	Stream<Envelope> launch(Map<GherkinEditorDocument, IStructuredSelection> selection, Mode mode);

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
