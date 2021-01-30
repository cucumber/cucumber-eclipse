package io.cucumber.eclipse.java.plugins;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.function.Consumer;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.internal.com.google.protobuf.Parser;

/**
 * open an server endpoint to communicate with a remote cucumber instance
 * 
 * @author christoph
 *
 */
public class MessageEndpoint {


	private ServerSocket serverSocket;
	private Consumer<Envelope> consumer;

	public MessageEndpoint(Consumer<Envelope> consumer) throws IOException {
		this.consumer = consumer;
		serverSocket = new ServerSocket(0);
	}

	private void closeSocket(ServerSocket serverSocket) {
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}

	public void start() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Socket socket = serverSocket.accept();
					try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
						int framelength;
						byte[] buffer = new byte[1024 * 1024 * 10];
						Parser<Envelope> parser = Envelope.parser();
						while ((framelength = inputStream.readInt()) > 0) {
							if (buffer.length < framelength) {
								buffer = new byte[framelength];
							}
							inputStream.readFully(buffer, 0, framelength);
							Envelope envelope = parser.parseFrom(buffer, 0, framelength);
							consumer.accept(envelope);
							if (envelope.hasTestRunFinished()) {
								break;
							}
						}
						// send goodby to the client...
						try (OutputStream stream = socket.getOutputStream()) {
							stream.write(CucumberEclipsePlugin.GOOD_BY_MESSAGE);
							stream.flush();
						}
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

	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}
}
