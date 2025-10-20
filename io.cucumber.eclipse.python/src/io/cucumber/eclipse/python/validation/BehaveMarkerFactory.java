package io.cucumber.eclipse.python.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import io.cucumber.eclipse.editor.marker.MarkerFactory;

/**
 * Utility class for creating Python/Behave specific markers
 */
public class BehaveMarkerFactory {

	/**
	 * Creates markers for unmatched steps (steps without matching glue code)
	 * 
	 * @param resource the resource to mark
	 * @param lineNumbers list of line numbers where unmatched steps are located
	 * @param snippetType the type identifier for the snippet
	 * @param persistent whether markers should persist
	 * @throws CoreException if marker creation fails
	 */
	public static void unmatchedSteps(IResource resource, List<Integer> lineNumbers, 
			String snippetType, boolean persistent) throws CoreException {
		
		// Delete existing unmatched step markers
		IMarker[] existingMarkers = resource.findMarkers(MarkerFactory.UNMATCHED_STEP, true, IResource.DEPTH_ZERO);
		for (IMarker marker : existingMarkers) {
			String source = marker.getAttribute(IMarker.SOURCE_ID, "");
			if (source.startsWith(snippetType + "_")) {
				marker.delete();
			}
		}
		
		// Create new markers for unmatched steps
		for (int lineNumber : lineNumbers) {
			String sourceId = snippetType + "_" + lineNumber;
			IMarker marker = resource.createMarker(MarkerFactory.UNMATCHED_STEP);
			marker.setAttribute(IMarker.SOURCE_ID, sourceId);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IMarker.MESSAGE, "Step does not have a matching glue code");
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.TRANSIENT, !persistent);
		}
	}
	
	/**
	 * Creates an error marker indicating that behave execution failed
	 * 
	 * @param resource the resource to mark
	 * @param message the error message
	 * @throws CoreException if marker creation fails
	 */
	public static void behaveExecutionError(IResource resource, String message) throws CoreException {
		// Delete existing behave execution error markers
		IMarker[] existingMarkers = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		for (IMarker marker : existingMarkers) {
			String source = marker.getAttribute(IMarker.SOURCE_ID, "");
			if ("behave_execution_error".equals(source)) {
				marker.delete();
			}
		}
		
		// Create new error marker
		IMarker marker = resource.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.SOURCE_ID, "behave_execution_error");
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.LINE_NUMBER, 1);
		marker.setAttribute(IMarker.TRANSIENT, true);
	}
}
