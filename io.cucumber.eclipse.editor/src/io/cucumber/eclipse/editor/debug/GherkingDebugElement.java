package io.cucumber.eclipse.editor.debug;

import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

import io.cucumber.eclipse.editor.Activator;

/**
 * Base class for all debug related elements
 * 
 * @author christoph
 *
 */
public class GherkingDebugElement extends DebugElement {

	public GherkingDebugElement(IDebugTarget target) {
		super(target);
	}

	@Override
	public String getModelIdentifier() {
		return Activator.DEBUG_MODEL_ID;
	}

}
