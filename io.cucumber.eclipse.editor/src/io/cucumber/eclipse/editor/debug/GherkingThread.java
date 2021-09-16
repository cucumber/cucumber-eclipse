package io.cucumber.eclipse.editor.debug;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

/**
 * Represents the single thread handling the events
 * 
 * @author christoph
 *
 */
public class GherkingThread extends GherkingDebugElement implements IThread {

	private volatile SuspendContext context;

	GherkingThread(IDebugTarget target) {
		super(target);
	}

	@Override
	public boolean canResume() {
		return context != null;
	}

	@Override
	public boolean canSuspend() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuspended() {
		return context != null;
	}

	@Override
	public void resume() throws DebugException {
		context().ifPresent(c -> {
			context = null;
			c.latch.countDown();
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
		});

	}

	private Optional<SuspendContext> context() {
		return Optional.ofNullable(context);
	}

	@Override
	public void suspend() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canStepInto() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepOver() {
		return false;
	}

	@Override
	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stepInto() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepOver() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepReturn() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		return context().map(c -> c.stackFrames).orElseGet(() -> new IStackFrame[0]);
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		return context().filter(c -> c.stackFrames.length > 0).isPresent();
	}

	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		IStackFrame[] frames = getStackFrames();
		if (frames.length > 0) {
			return frames[0];
		}
		return null;
	}

	@Override
	public String getName() throws DebugException {
		return "Cucumber-Event-Thread";
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		return context().map(SuspendContext::getBreakPoints).orElseGet(() -> new IBreakpoint[0]);
	}

	public synchronized CountDownLatch suspend(IBreakpoint breakpoint, IStackFrame[] stackFrames) {
		if (context != null) {
			throw new IllegalStateException();
		}
		context = new SuspendContext(breakpoint, stackFrames);
		fireSuspendEvent(DebugEvent.BREAKPOINT);
		return context.latch;
	}

	public CountDownLatch suspend(IStackFrame[] stackFrames, int detail) {
		if (context != null) {
			throw new IllegalStateException();
		}
		context = new SuspendContext(null, stackFrames);
		fireSuspendEvent(detail);
		return context.latch;
	}

	private static final class SuspendContext {
		private IBreakpoint breakpoint;
		private IStackFrame[] stackFrames;

		public SuspendContext(IBreakpoint breakpoint, IStackFrame[] stackFrames) {
			this.breakpoint = breakpoint;
			this.stackFrames = stackFrames;
		}

		public IBreakpoint[] getBreakPoints() {
			if (breakpoint == null) {
				return null;
			}
			return new IBreakpoint[] { breakpoint };
		}

		private CountDownLatch latch = new CountDownLatch(1);
	}

}
