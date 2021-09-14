package io.cucumber.eclipse.editor.launching;

/**
 * a provider that is capable of supply a listener with envelopes
 * 
 * @author christoph
 *
 */
public interface EnvelopeProvider {

	void addEnvelopeListener(EnvelopeListener listener);

	void removeEnvelopeListener(EnvelopeListener listener);
}
