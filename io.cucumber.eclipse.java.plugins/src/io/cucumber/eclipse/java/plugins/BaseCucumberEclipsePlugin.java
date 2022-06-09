package io.cucumber.eclipse.java.plugins;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.cucumber.eclipse.java.plugins.dto.Envelope;


public abstract class BaseCucumberEclipsePlugin<T> {

	protected Consumer<T> consumer;
	public static final int HANDLED_MESSAGE = 0x1;
	public static final int GOOD_BY_MESSAGE = 0x0;

	public BaseCucumberEclipsePlugin(Consumer<T> consumer) {
		this.consumer = consumer;
	}
	public BaseCucumberEclipsePlugin(String port) throws IOException {
		this.consumer = new SocketConsumer<T>(port, this) ;
	}
	
	protected static final class SocketConsumer<T> implements Consumer<T> {

		private final Socket socket;
		private final DataOutputStream output;
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024 * 1024 * 10);
		private AtomicInteger written = new AtomicInteger();
		private InputStream input;
		private BaseCucumberEclipsePlugin<T> plugin;

		public SocketConsumer(String port,BaseCucumberEclipsePlugin<T> plugin) throws IOException {
			this.plugin = plugin;
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
		public void accept(T env) {
			try {
			synchronized (socket) {
				if (socket.isClosed()) {
					return;
				}
				try {
					buffer.reset();
					ObjectOutputStream oos = new ObjectOutputStream(buffer);
					Envelope convert = plugin.convert(env);
					oos.writeObject(convert);
					output.writeInt(buffer.size());
					buffer.writeTo(output);
					output.flush();
					int read = input.read() & 0xFF;
					written.incrementAndGet();
					if (plugin.isFinished(env)|| read == GOOD_BY_MESSAGE) {
						finish();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}catch (Throwable e) {
					e.printStackTrace();
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void finish() throws IOException {
			output.writeInt(GOOD_BY_MESSAGE);
			output.flush();
			input.read();
			socket.close();
		}

	}

	protected abstract boolean isFinished(T env);

	protected abstract io.cucumber.eclipse.java.plugins.dto.Envelope convert(T envelope);

	

}