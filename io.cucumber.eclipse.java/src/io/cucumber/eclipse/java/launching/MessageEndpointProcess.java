package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import io.cucumber.eclipse.editor.launching.EnvelopeListener;
import io.cucumber.eclipse.editor.launching.EnvelopeProvider;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.plugins.MessageEndpoint;
import io.cucumber.messages.Messages.Envelope;

/**
 * Integrates a {@link MessageEndpoint} with the eclipse launcher framework
 * 
 * @author christoph
 *
 */
public class MessageEndpointProcess extends MessageEndpoint implements IProcess, EnvelopeProvider {

	private ILaunch launch;
	private List<Envelope> envelopes = new ArrayList<>();
	private List<EnvelopeListener> consumers = new ArrayList<>();
	private Map<String, String> attributes = new HashMap<>();

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
		return "Cucumber";
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
	protected synchronized void handleMessage(Envelope envelope) {
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

	@Override
	public synchronized void addEnvelopeListener(EnvelopeListener listener) {
		if (!consumers.contains(listener)) {
			consumers.add(listener);
			for (Envelope envelope : envelopes) {
				listener.handleEnvelope(envelope);
			}
		}
	}

	@Override
	public synchronized void removeEnvelopeListener(EnvelopeListener listener) {
		consumers.remove(listener);
	}

}
