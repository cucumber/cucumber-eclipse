package io.cucumber.eclipse.editor.debug;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;

/**
 * Debug Target for Gherking
 * 
 * @author christoph
 *
 * @param <Process>
 */
public class GherkingDebugTarget<Process extends IProcess & ISuspendResume & IDisconnect> extends GherkingDebugElement
		implements IDebugTarget {

	private Process endpointProcess;
	private ILaunch launch;
	private GherkingThread thread;
	private String name;

	public GherkingDebugTarget(ILaunch launch, Process endpointProcess, String name) {
		super(null);
		this.launch = launch;
		this.endpointProcess = endpointProcess;
		this.name = name;
		this.thread = new GherkingThread(this);
	}

	public GherkingThread getThread() {
		return thread;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public String getModelIdentifier() {
		return GherkingBreakpoint.MODEL_ID;
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public boolean canTerminate() {
		return endpointProcess.isTerminated();
	}

	@Override
	public boolean isTerminated() {
		return endpointProcess.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		endpointProcess.terminate();

	}

	@Override
	public boolean canResume() {
		return endpointProcess.canResume();
	}

	@Override
	public boolean canSuspend() {
		return endpointProcess.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return endpointProcess.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		endpointProcess.resume();
		fireResumeEvent(DebugEvent.CLIENT_REQUEST);
	}

	@Override
	public void suspend() throws DebugException {
		endpointProcess.suspend();
		fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean canDisconnect() {
		return endpointProcess.canDisconnect();
	}

	@Override
	public void disconnect() throws DebugException {
		endpointProcess.disconnect();
	}

	@Override
	public boolean isDisconnected() {
		return endpointProcess.isDisconnected();
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, getClass(), "not supported"));
	}

	@Override
	public Process getProcess() {
		return endpointProcess;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		if (isTerminated()) {
			return new IThread[0];
		}
		return new IThread[] { thread };
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return true;
	}

	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return breakpoint instanceof GherkingBreakpoint;
	}

}
