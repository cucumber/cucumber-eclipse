package io.cucumber.eclipse.editor.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

import io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList.StepMatchArgument.Group;

/**
 * A group value
 * 
 * @author christoph
 *
 */
public class GherkingGroupValue extends GherkingValue {

	private Group group;

	public GherkingGroupValue(IDebugElement parent, String type, Group group) {
		super(parent, type);
		this.group = group;
	}

	@Override
	public String getValueString() throws DebugException {
		return group.getValue();
	}

}
