package io.cucumber.eclipse.java.plugins;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.internal.com.google.protobuf.Parser;

/**
 * open an server endpoint to communicate with a remote cucumber instance
 * 
 * @author christoph
 *
 */
public abstract class MessageEndpoint {

	private ServerSocket serverSocket;

	public MessageEndpoint() throws IOException {
		serverSocket = new ServerSocket(0);
	}

	private void closeSocket(ServerSocket serverSocket) {
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}

	protected abstract void handleMessage(Envelope envelope) throws InterruptedException;

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
						Parser<Envelope> parser = Envelope.parser();
						while ((framelength = inputStream.readInt()) > 0) {
							if (buffer.length < framelength) {
								buffer = new byte[framelength];
							}
							inputStream.readFully(buffer, 0, framelength);
							Envelope envelope = parser.parseFrom(buffer, 0, framelength);
							try {
								handleMessage(envelope);
							} catch (InterruptedException e) {
								break;
							}
							outputStream.write(CucumberEclipsePlugin.HANDLED_MESSAGE);
							outputStream.flush();
							if (envelope.hasTestRunFinished()) {
								break;
							}
						}
						outputStream.write(CucumberEclipsePlugin.GOOD_BY_MESSAGE);
						outputStream.flush();
					}
					socket.close();
				} catch (IOException e) {
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

	public void addArguments(Collection<String> args) {
		args.add("-p");
		args.add(CucumberEclipsePlugin.class.getName() + ":" + String.valueOf(serverSocket.getLocalPort()));
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
}
