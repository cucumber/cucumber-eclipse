package io.cucumber.eclipse.editor.launching;

import io.cucumber.messages.types.Envelope;

/**
 * A listener that is notified about new envelopes
 * @author christoph
 *
 */
public interface EnvelopeListener {

	/**
	 * Handles the given {@link Envelope}
	 * 
	 * @param envelope
	 */
	void handleEnvelope(Envelope envelope);
}
