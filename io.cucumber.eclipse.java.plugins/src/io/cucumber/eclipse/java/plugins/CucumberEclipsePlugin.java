package io.cucumber.eclipse.java.plugins;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

/**
 * Specialized plugin that allows to transfer the {@link Envelope}s emitted to a
 * remote process or consume them locally
 * 
 * @author christoph
 *
 */
public class CucumberEclipsePlugin implements ConcurrentEventListener {

	private Consumer<Envelope> consumer;
	public static final int HANDLED_MESSAGE = 0x1;
	public static final int GOOD_BY_MESSAGE = 0x0;

	public CucumberEclipsePlugin(String port) throws IOException {
		this(new SocketConsumer(port));
	}

	public CucumberEclipsePlugin(Consumer<Envelope> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(Envelope.class, this::writeMessage);
	}

	private void writeMessage(Envelope envelope) {
		consumer.accept(envelope);
	}

	private static final class SocketConsumer implements Consumer<Envelope> {

		private final Socket socket;
		private final DataOutputStream output;
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024 * 1024 * 10);
		private AtomicInteger written = new AtomicInteger();
		private InputStream input;

		public SocketConsumer(String port) throws NumberFormatException, UnknownHostException, IOException {
			try {
				socket = new Socket((String) null, Integer.parseInt(port));
				output = new DataOutputStream(socket.getOutputStream());
				input = socket.getInputStream();
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							if (!socket.isClosed()) {
								output.writeInt(0);
								socket.close();
							}
						} catch (IOException e) {
						}

					}
				}));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("can't parse port number", e);
			}
		}

		@Override
		public void accept(Envelope env) {
			synchronized (socket) {
				if (socket.isClosed()) {
					return;
				}
				try {
					buffer.reset();
					env.writeTo(buffer);
					output.writeInt(buffer.size());
					buffer.writeTo(output);
					output.flush();
					int read = input.read() & 0xFF;
					written.incrementAndGet();
					if (env.hasTestRunFinished() || read == GOOD_BY_MESSAGE) {
						finish();
					}
				} catch (IOException e) {
				}
			}

		}

		private void finish() throws IOException {
			output.writeInt(GOOD_BY_MESSAGE);
			output.flush();
			input.read();
			socket.close();
		}

	}
}
