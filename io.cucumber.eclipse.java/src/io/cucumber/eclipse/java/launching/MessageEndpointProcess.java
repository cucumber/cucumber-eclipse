package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.ISuspendResume;

import io.cucumber.eclipse.editor.launching.EnvelopeListener;
import io.cucumber.eclipse.editor.launching.EnvelopeProvider;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.plugins.MessageEndpoint;
import io.cucumber.messages.types.Envelope;

/**
 * Integrates a {@link MessageEndpoint} with the eclipse launcher framework
 * 
 * @author christoph
 *
 */
public class MessageEndpointProcess extends MessageEndpoint
		implements IProcess, EnvelopeProvider, ISuspendResume, IDisconnect {

	private ILaunch launch;
	private List<Envelope> envelopes = new ArrayList<>();
	private List<EnvelopeListener> consumers = new ArrayList<>();
	private Map<String, String> attributes = new HashMap<>();
	private volatile boolean suspended;
	private IBreakpoint[] breakpoints;

	public MessageEndpointProcess(ILaunch launch) throws IOException {
		this.launch = launch;
		launch.addProcess(this);
		attributes.put(IProcess.ATTR_PROCESS_TYPE, "cucumber-message-endpoint");
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean canTerminate() {
		return false;
	}

	@Override
	public String getLabel() {
		return "Cucumber Message Listener";
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return null;
	}

	@Override
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public int getExitValue() throws DebugException {
		return 0;
	}

	@Override
	protected void handleMessage(Envelope envelope) throws InterruptedException {
		synchronized (this) {
			while (suspended) {
				wait();
			}
			envelopes.add(envelope);
			for (EnvelopeListener consumer : consumers) {
				try {
					consumer.handleEnvelope(envelope);
				} catch (RuntimeException e) {
					Activator.getDefault().getLog()
							.error("Listener throws RuntimeException while handling Envelope " + envelope, e);
				}
			}
		}

	}

	@Override
	public void addEnvelopeListener(EnvelopeListener listener) {
		synchronized (this) {
			if (!consumers.contains(listener)) {
				consumers.add(listener);
				for (Envelope envelope : envelopes) {
					listener.handleEnvelope(envelope);
				}
			}
		}
	}

	@Override
	public synchronized void removeEnvelopeListener(EnvelopeListener listener) {
		synchronized (this) {
			consumers.remove(listener);
		}
	}

	@Override
	public boolean canDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canResume() {
		return isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !isSuspended();
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public void resume() throws DebugException {
		suspended = false;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public void suspend() throws DebugException {
		suspended = true;
	}

}
