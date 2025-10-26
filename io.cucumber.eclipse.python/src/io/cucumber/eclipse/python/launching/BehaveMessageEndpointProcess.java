package io.cucumber.eclipse.python.launching;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.ISuspendResume;

import io.cucumber.eclipse.editor.launching.EnvelopeListener;
import io.cucumber.eclipse.editor.launching.EnvelopeProvider;
import io.cucumber.eclipse.java.plugins.Jackson;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.messages.types.Envelope;

/**
 * Message endpoint for Python/Behave that receives Cucumber messages via socket.
 * 
 * This class provides the same functionality as the Java MessageEndpointProcess,
 * receiving Cucumber messages from the Python behave_cucumber_eclipse formatter
 * and making them available to Eclipse listeners.
 * 
 * @author copilot
 */
public class BehaveMessageEndpointProcess implements IProcess, EnvelopeProvider, ISuspendResume, IDisconnect {

	private static final int HANDLED_MESSAGE = 0x01;
	private static final int GOOD_BY_MESSAGE = 0x00;
	
	private ServerSocket serverSocket;
	private ILaunch launch;
	private List<Envelope> envelopes = new ArrayList<>();
	private List<EnvelopeListener> consumers = new ArrayList<>();
	private Map<String, String> attributes = new HashMap<>();
	private volatile boolean suspended;

	public BehaveMessageEndpointProcess(ILaunch launch) throws IOException {
		this.serverSocket = new ServerSocket(0);
		this.launch = launch;
		launch.addProcess(this);
		attributes.put(IProcess.ATTR_PROCESS_TYPE, "behave-message-endpoint");
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
		return "Behave Message Listener";
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
	
	/**
	 * Start listening for incoming messages
	 */
	public void start() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Socket socket = serverSocket.accept();
					try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
							OutputStream outputStream = socket.getOutputStream()) {
						int framelength;
						byte[] buffer = new byte[1024 * 1024 * 10];
						while ((framelength = inputStream.readInt()) > 0) {
							if (buffer.length < framelength) {
								buffer = new byte[framelength];
							}
							inputStream.readFully(buffer, 0, framelength);
							Envelope envelope = Jackson.OBJECT_MAPPER.readerFor(Envelope.class).readValue(buffer, 0,
									framelength);
							try {
								handleMessage(envelope);
							} catch (InterruptedException e) {
								break;
							}
							outputStream.write(HANDLED_MESSAGE);
							outputStream.flush();
							if (envelope.getTestRunFinished().isPresent()) {
								break;
							}
						}
						outputStream.write(GOOD_BY_MESSAGE);
						outputStream.flush();
					}
					socket.close();
				} catch (IOException e) {
					// Connection closed or error
				} finally {
					try {
						serverSocket.close();
					} catch (IOException e) {
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	public boolean isTerminated() {
		return serverSocket.isClosed();
	}

	public void terminate() {
		try {
			serverSocket.close();
		} catch (IOException e) {
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
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
		// Not implemented
	}

	@Override
	public boolean isDisconnected() {
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
	
	/**
	 * Adds formatter arguments to the behave command to enable Eclipse integration.
	 * This sets the formatter and port for the socket connection.
	 * 
	 * @param args List of command arguments to add to
	 */
	public void addBehaveArguments(List<String> args) {
		// Add the formatter argument
		// Format: --format module_name:ClassName
		args.add("--format");
		args.add("behave_cucumber_eclipse:CucumberEclipseFormatter");
		
		// Add port via userdata (behave's -D option)
		args.add("-D");
		args.add("cucumber_eclipse_port=" + serverSocket.getLocalPort());
	}
}
