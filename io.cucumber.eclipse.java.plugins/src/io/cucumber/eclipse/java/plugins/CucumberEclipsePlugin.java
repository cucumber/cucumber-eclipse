package io.cucumber.eclipse.java.plugins;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.cucumber.messages.MessageToNdjsonWriter;
import io.cucumber.messages.MessageToNdjsonWriter.Serializer;
import io.cucumber.messages.types.Envelope;
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

	private static final Serializer SERIALIZER;

	static {
		SERIALIZER = getSerializer();
	}

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
					try (MessageToNdjsonWriter writer = new MessageToNdjsonWriter(buffer, SERIALIZER)) {
						writer.write(env);
					}
					output.writeInt(buffer.size());
					buffer.writeTo(output);
					output.flush();
					int read = input.read() & 0xFF;
					written.incrementAndGet();
					if (env.getTestRunFinished().isPresent() || read == GOOD_BY_MESSAGE) {
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

	private static Serializer getSerializer() {
		try {
			// TODO workaround for https://github.com/cucumber/cucumber-jvm/pull/3102
			Class<?> clazz = CucumberEclipsePlugin.class.getClassLoader().loadClass("io.cucumber.core.plugin.Jackson");
			Field field = clazz.getField("OBJECT_MAPPER");
			field.setAccessible(true);
			Object object = field.get(null);
			Method method = object.getClass().getMethod("writeValue", Writer.class, Object.class);
			return new Serializer() {

				@Override
				public void writeValue(Writer writer, Envelope value) throws IOException {
					try {
						method.invoke(object, writer, value);
					} catch (IllegalAccessException e) {
						throw new IOException(e);
					} catch (IllegalArgumentException e) {
						throw new IOException(e);
					} catch (InvocationTargetException e) {
						Throwable cause = e.getCause();
						if (cause instanceof IOException) {
							throw (IOException) cause;
						}
						if (cause instanceof RuntimeException) {
							throw (RuntimeException) cause;
						}
						throw new IOException(e);
					}
				}
			};
		} catch (Exception e) {
		}
		return new Jackson();
	}
}
