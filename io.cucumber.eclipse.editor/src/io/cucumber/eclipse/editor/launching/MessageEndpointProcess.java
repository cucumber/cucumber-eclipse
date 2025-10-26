package io.cucumber.eclipse.editor.launching;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.ISuspendResume;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.messages.types.Envelope;

/**
 * Generic message endpoint process that receives Cucumber messages via socket.
 * 
 * This class can be extended by backend-specific implementations (Java, Python, etc.)
 * to provide real-time test execution monitoring in Eclipse.
 * 
 * @author copilot
 */
public abstract class MessageEndpointProcess implements IProcess, EnvelopeProvider, ISuspendResume, IDisconnect {

	private static final int HANDLED_MESSAGE = 0x01;
	private static final int GOOD_BY_MESSAGE = 0x00;
	
	private ServerSocket serverSocket;
	private ILaunch launch;
	private List<Envelope> envelopes = new ArrayList<>();
	private List<EnvelopeListener> consumers = new ArrayList<>();
	private Map<String, String> attributes = new HashMap<>();
	private volatile boolean suspended;

	public MessageEndpointProcess(ILaunch launch) throws IOException {
		this.serverSocket = new ServerSocket(0);
		this.launch = launch;
		launch.addProcess(this);
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
	public abstract String getLabel();

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
					ILog.get().error("Listener throws RuntimeException while handling Envelope " + envelope, e);
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
							Envelope envelope = deserializeEnvelope(buffer, framelength);
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
	
	/**
	 * Deserialize an envelope from bytes.
	 * Backend-specific implementations should override this to use their preferred JSON library.
	 * 
	 * @param buffer byte buffer containing the message
	 * @param length length of the message in the buffer
	 * @return deserialized Envelope
	 * @throws IOException if deserialization fails
	 */
	protected abstract Envelope deserializeEnvelope(byte[] buffer, int length) throws IOException;
	
	/**
	 * Get the port number that the server socket is listening on
	 * 
	 * @return port number
	 */
	public int getPort() {
		return serverSocket.getLocalPort();
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
}
