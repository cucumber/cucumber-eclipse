package io.cucumber.eclipse.editor;

import java.io.IOException;

import io.cucumber.messages.types.Envelope;

/**
 * Service interface for reading Envelope messages from byte arrays.
 * Implementations provide the actual deserialization logic.
 */
public interface EnvelopeReader {
	
	/**
	 * Reads an Envelope from a byte buffer.
	 * 
	 * @param buffer the byte buffer containing the envelope data
	 * @param length the number of bytes to read from the buffer
	 * @return the deserialized Envelope
	 * @throws IOException if an I/O error occurs during reading or the
	 *                     {@link Envelope} can not be deserialized
	 */
	Envelope readEnvelope(byte[] buffer, int length) throws IOException;
}
