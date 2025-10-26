package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

import io.cucumber.eclipse.java.plugins.CucumberEclipsePlugin;

/**
 * Integrates message endpoint with the Eclipse launcher framework for Java/JVM.
 * 
 * This class extends the generic MessageEndpointProcess and provides Java-specific
 * functionality for receiving messages from the Java CucumberEclipsePlugin.
 * 
 * @author christoph
 */
public class JavaMessageEndpointProcess extends io.cucumber.eclipse.editor.launching.MessageEndpointProcess {

	public JavaMessageEndpointProcess(ILaunch launch) throws IOException {
		super(launch);
		setAttribute(IProcess.ATTR_PROCESS_TYPE, "cucumber-message-endpoint");
	}

	@Override
	public String getLabel() {
		return "Cucumber Message Listener";
	}
	
	/**
	 * Adds Java-specific plugin arguments to enable Eclipse integration.
	 * This adds the CucumberEclipsePlugin with the port number.
	 * 
	 * @param args Collection of command arguments to add to
	 */
	public void addArguments(Collection<String> args) {
		args.add("-p");
		args.add(CucumberEclipsePlugin.class.getName() + ":" + String.valueOf(getPort()));
	}
}
