package io.cucumber.eclipse.editor.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

/**
 * Gherking line breakpoint implementation to allow stepping through the feature
 * code
 * 
 * @author christoph
 *
 */
public class GherkingBreakpoint extends LineBreakpoint {

	public static final String MODEL_ID = GherkingBreakpoint.class.getName();

	public GherkingBreakpoint() {
	}

	public GherkingBreakpoint(IResource resource, int lineNumber) throws CoreException {
		IMarker marker = resource.createMarker("io.cucumber.eclipse.editor.lineBreakpoint.marker");
		setMarker(marker);
		setEnabled(true);
		ensureMarker().setAttribute(IMarker.LINE_NUMBER, lineNumber);
		ensureMarker().setAttribute(IBreakpoint.ID, MODEL_ID);
	}

	@Override
	public String getModelIdentifier() {
		return MODEL_ID;
	}

}
