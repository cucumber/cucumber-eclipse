package io.cucumber.eclipse.java.plugins;

import java.io.IOException;
import java.util.function.Consumer;

//import com.google.gson.Gson;

import io.cucumber.messages.types.Envelope;

/**
 * Specialized plugin that allows to transfer the {@link Envelope}s emitted to a
 * remote process or consume them locally
 * 
 * @author christoph
 *
 */
public  class Cucumber7EclipsePlugin extends BaseCucumberEclipsePlugin  {

	public Cucumber7EclipsePlugin(String port) throws IOException {
		super(port);
	}

	

	public Cucumber7EclipsePlugin(Consumer<Envelope> consumer) {
		super(consumer);
	}



	@Override
	protected io.cucumber.eclipse.java.plugins.dto.Envelope convert(
			Envelope envelope) {
		
		return MessageToDtoConverter.convert(envelope);
	}
}
