package io.cucumber.eclipse.java.launching;

import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

import io.cucumber.eclipse.java.plugins.Jackson;
import io.cucumber.messages.types.Envelope;

/**
 * Integrates message endpoint with the Eclipse launcher framework for Java/JVM.
 * 
 * This class extends the generic MessageEndpointProcess and provides Java-specific
 * functionality for receiving messages from the Java CucumberEclipsePlugin.
 * 
 * @author christoph
 */
public class MessageEndpointProcess extends io.cucumber.eclipse.editor.launching.MessageEndpointProcess {

	public MessageEndpointProcess(ILaunch launch) throws IOException {
		super(launch);
		setAttribute(IProcess.ATTR_PROCESS_TYPE, "cucumber-message-endpoint");
	}

	@Override
	public String getLabel() {
		return "Cucumber Message Listener";
	}

	@Override
	protected Envelope deserializeEnvelope(byte[] buffer, int length) throws IOException {
		return Jackson.OBJECT_MAPPER.readerFor(Envelope.class).readValue(buffer, 0, length);
	}
}
