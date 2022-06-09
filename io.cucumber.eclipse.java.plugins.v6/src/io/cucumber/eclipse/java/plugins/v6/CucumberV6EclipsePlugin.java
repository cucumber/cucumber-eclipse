package io.cucumber.eclipse.java.plugins.v6;

import java.io.IOException;
import java.util.function.Consumer;

import io.cucumber.eclipse.java.plugins.BaseCucumberEclipsePlugin;
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
public class CucumberV6EclipsePlugin extends BaseCucumberEclipsePlugin<Envelope> implements ConcurrentEventListener{


	public CucumberV6EclipsePlugin(String port) throws IOException {
		super(port);
	}

	public CucumberV6EclipsePlugin(Consumer<Envelope> consumer) {
		super(consumer);
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(Envelope.class, consumer::accept);
	}

	@Override
	protected boolean isFinished(Envelope env) {
		return env.hasTestRunFinished() ;
	}

	@Override
	protected io.cucumber.eclipse.java.plugins.dto.Envelope convert(Envelope envelope) {
		return MessageToDtoConverter.convert(envelope);
	}

	
}
