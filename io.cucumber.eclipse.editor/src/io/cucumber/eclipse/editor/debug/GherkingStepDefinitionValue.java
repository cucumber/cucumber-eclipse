package io.cucumber.eclipse.editor.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

import io.cucumber.messages.types.StepDefinition;

/**
 * A value representing a step definition
 * 
 * @author christoph
 *
 */
public class GherkingStepDefinitionValue extends GherkingValue {

	private StepDefinition definition;
	private String parsedString;

	public GherkingStepDefinitionValue(IDebugElement parent, StepDefinition definition, String parsedString) {
		super(parent, "Step Definition");
		this.definition = definition;
		this.parsedString = parsedString;
	}

	@Override
	public String getValueString() throws DebugException {
		return parsedString;
	}

}
