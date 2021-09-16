package io.cucumber.eclipse.editor.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * a simple variable implementation
 * 
 * @author christoph
 *
 */
public class GherkingStepVariable extends GherkingDebugElement implements IVariable {

	private IStackFrame frame;
	private String name;
	private IValue value;

	public GherkingStepVariable(IStackFrame frame, String name, IValue value) {
		super(frame.getDebugTarget());
		this.frame = frame;
		this.name = name;
		this.value = value;
	}

	@Override
	public void setValue(String expression) throws DebugException {

	}

	@Override
	public void setValue(IValue value) throws DebugException {

	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

	@Override
	public IValue getValue() throws DebugException {
		return value;
	}

	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return getValue().getReferenceTypeName();
	}

	@Override
	public boolean hasValueChanged() throws DebugException {
		return false;
	}

}
