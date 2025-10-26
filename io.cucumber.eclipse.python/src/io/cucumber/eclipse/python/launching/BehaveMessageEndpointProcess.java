package io.cucumber.eclipse.python.launching;

import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

import io.cucumber.eclipse.editor.launching.MessageEndpointProcess;
import io.cucumber.eclipse.java.plugins.Jackson;
import io.cucumber.messages.types.Envelope;

/**
 * Message endpoint for Python/Behave that receives Cucumber messages via socket.
 * 
 * This class extends the generic MessageEndpointProcess and provides Behave-specific
 * functionality for receiving messages from the Python behave_cucumber_eclipse formatter.
 * 
 * @author copilot
 */
public class BehaveMessageEndpointProcess extends MessageEndpointProcess {

	public BehaveMessageEndpointProcess(ILaunch launch) throws IOException {
		super(launch);
		setAttribute(IProcess.ATTR_PROCESS_TYPE, "behave-message-endpoint");
	}

	@Override
	public String getLabel() {
		return "Behave Message Listener";
	}

	@Override
	protected Envelope deserializeEnvelope(byte[] buffer, int length) throws IOException {
		return Jackson.OBJECT_MAPPER.readerFor(Envelope.class).readValue(buffer, 0, length);
	}
}
