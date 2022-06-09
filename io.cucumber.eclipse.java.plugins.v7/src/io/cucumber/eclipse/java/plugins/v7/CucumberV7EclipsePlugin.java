package io.cucumber.eclipse.java.plugins.v7;

import java.io.IOException;
import java.util.function.Consumer;

import io.cucumber.eclipse.java.plugins.BaseCucumberEclipsePlugin;

//import com.google.gson.Gson;

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
public  class CucumberV7EclipsePlugin extends BaseCucumberEclipsePlugin<Envelope>  implements ConcurrentEventListener {


	public CucumberV7EclipsePlugin(String port) throws IOException {
		super(port);
	}

	public CucumberV7EclipsePlugin(Consumer<Envelope> consumer) {
		super(consumer);
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(Envelope.class, consumer::accept );
	}

	@Override
	protected boolean isFinished(Envelope env) {
		return env.getTestRunFinished().isPresent() ;
	}

	@Override
	protected io.cucumber.eclipse.java.plugins.dto.Envelope convert(Envelope envelope) {
		return MessageToDtoConverter.convert(envelope);
	}
}
