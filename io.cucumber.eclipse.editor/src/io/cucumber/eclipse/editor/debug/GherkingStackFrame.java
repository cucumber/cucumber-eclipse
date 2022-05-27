package io.cucumber.eclipse.editor.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * A Stack frame representation of Gherking Document
 * 
 * @author christoph
 *
 */
public class GherkingStackFrame extends GherkingDebugElement implements IStackFrame {

	private IThread thread;
	private int lineNumber;
	private String name;
	private List<IVariable> variables = new ArrayList<>();

	public GherkingStackFrame(IThread thread, int lineNumber, String name) {
		super(thread.getDebugTarget());
		this.thread = thread;
		this.lineNumber = lineNumber;
		this.name = name;
	}

	

	@Override
	public boolean canStepInto() {
		return false;
	}

	@Override
	public boolean canStepOver() {
		return false;
	}

	@Override
	public boolean canStepReturn() {
		return false;
	}

	@Override
	public boolean isStepping() {
		return thread.isStepping();
	}

	@Override
	public void stepInto() throws DebugException {
	}

	@Override
	public void stepOver() throws DebugException {

	}

	@Override
	public void stepReturn() throws DebugException {
	}

	@Override
	public boolean canResume() {
		return thread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return thread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return thread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		thread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		thread.suspend();
	}

	@Override
	public boolean canTerminate() {
		return thread.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return thread.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		thread.terminate();
	}

	@Override
	public IThread getThread() {
		return thread;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return variables.toArray(IVariable[]::new);
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return variables.size() > 0;
	}

	@Override
	public int getLineNumber() throws DebugException {
		return lineNumber;
	}

	@Override
	public int getCharStart() throws DebugException {
		return -1;
	}

	@Override
	public int getCharEnd() throws DebugException {
		return -1;
	}

	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return new IRegisterGroup[0];
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}

	public void addVariable(IVariable variable) {
		variables.add(variable);
	}

}
