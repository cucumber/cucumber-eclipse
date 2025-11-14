package io.cucumber.eclipse.python.launching;

import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

import io.cucumber.eclipse.editor.launching.MessageEndpointProcess;

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
}
