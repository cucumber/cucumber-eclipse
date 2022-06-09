package io.cucumber.eclipse.java.plugins;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;

import org.eclipse.core.runtime.Platform;

import io.cucumber.eclipse.java.plugins.v6.CucumberV6EclipsePlugin;
import io.cucumber.eclipse.java.plugins.v6.DtoToMessageConverter;
import io.cucumber.eclipse.java.plugins.v7.CucumberV7EclipsePlugin;
import io.cucumber.messages.Messages.Envelope;



/**
 * open an server endpoint to communicate with a remote cucumber instance
 * 
 * @author christoph
 *
 */
public abstract class MessageEndpoint {
	
	public static final Class<?> PLUGIN_CLASS = CucumberV7EclipsePlugin.class;
	
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
						//Parser<Envelope> parser = Envelope.parser();
						
						while ((framelength = inputStream.readInt()) > 0) {
							if (buffer.length < framelength) {
								buffer = new byte[framelength];
							}
							inputStream.readFully(buffer, 0, framelength);
							ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, framelength);
							ObjectInputStream ois = new ObjectInputStream(bais);
							Envelope envelope =null;
							try {
								io.cucumber.eclipse.java.plugins.dto.Envelope env = (io.cucumber.eclipse.java.plugins.dto.Envelope) ois
										.readObject();
								envelope = DtoToMessageConverter.convert(env);
								System.err.println(envelope.toString());
								try {
									handleMessage(envelope);
								} catch (InterruptedException e) {
									break;
								}
							} catch (Exception e) {
								Platform.getLog(this.getClass()).error("error reading Envelope from socket", e);
							}
							outputStream.write(BaseCucumberEclipsePlugin.HANDLED_MESSAGE);
							outputStream.flush();
							if (envelope != null && envelope.hasTestRunFinished()) {
								break;
							}
							

						}
						outputStream.write(BaseCucumberEclipsePlugin.GOOD_BY_MESSAGE);
						outputStream.flush();
					} 
					socket.close();
				} catch (IOException e) {
					Platform.getLog(this.getClass()).error("error reading", e);
				} finally {
					try {
						serverSocket.close();
					} catch (IOException e) {
						Platform.getLog(this.getClass()).error("error closing", e);
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public void addArguments(Collection<String> args) {
		args.add("-p");
		args.add(PLUGIN_CLASS.getName() + ":" + String.valueOf(serverSocket.getLocalPort()));
	}

	public boolean isTerminated() {
		return serverSocket.isClosed();
	}

	public void terminate() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Platform.getLog(this.getClass()).error("error closing", e);
		}
	}
}
