package io.cucumber.eclipse.editor.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * base class for values
 * 
 * @author christoph
 *
 */
public abstract class GherkingValue extends GherkingDebugElement implements IValue {

	private List<IVariable> variables = new ArrayList<>();
	private String typeName;

	public GherkingValue(IDebugElement parent, String typeName) {
		super(parent.getDebugTarget());
		this.typeName = typeName;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return typeName;
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return variables.toArray(IVariable[]::new);
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return variables.size() > 0;
	}

	public void addVariable(IVariable variable) {
		variables.add(variable);
	}

}
