package io.cucumber.eclipse.editor.document;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

/**
 * Document setup participant that registers Gherkin feature documents with the
 * manager.
 * <p>
 * This class is automatically invoked by Eclipse when a feature file document
 * is created. It implements {@link IDocumentListener} to track document changes
 * and notify the {@link GherkinEditorDocumentManager} about document lifecycle
 * events.
 * </p>
 * <p>
 * The participant:
 * <ul>
 * <li>Registers itself as a document listener to track changes</li>
 * <li>Notifies the manager when a document is set up (created)</li>
 * <li>Notifies the manager when a document changes</li>
 * <li>Uses a single listener instance per document to avoid duplicates</li>
 * </ul>
 * </p>
 * 
 * @author christoph
 */
public class GherkinDocumentSetupParticipant
		implements IDocumentSetupParticipant, IDocumentListener, IFileBufferListener {

	// TODO make IDocumentSetupParticipant a service and allow registering listeners
	// by whiteboard service

	public GherkinDocumentSetupParticipant() {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(this);
	}

	/**
	 * Called by Eclipse when a feature file document is created.
	 * <p>
	 * Registers this instance as a document listener and notifies the manager about
	 * the new document.
	 * </p>
	 * 
	 * @param document the document being set up
	 */
	@Override
	public void setup(IDocument document) {
		if (GherkinEditorDocumentManager.isCompatibleTextBuffer(document)) {
			document.addDocumentListener(this);
			GherkinEditorDocumentManager.setupTextBuffer(document);
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		GherkinEditorDocumentManager.textBufferChanged(event.getDocument());
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	@Override
	public void bufferCreated(IFileBuffer buffer) {

	}

	@Override
	public void bufferDisposed(IFileBuffer buffer) {
		if (buffer instanceof ITextFileBuffer textbuffer) {
			IDocument document = textbuffer.getDocument();
			GherkinEditorDocumentManager.textBufferRemoved(document);
		}
	}

	@Override
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
	}

	@Override
	public void bufferContentReplaced(IFileBuffer buffer) {
	}

	@Override
	public void stateChanging(IFileBuffer buffer) {
	}

	@Override
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
	}

	@Override
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
	}

	@Override
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
	}

	@Override
	public void underlyingFileDeleted(IFileBuffer buffer) {
	}

	@Override
	public void stateChangeFailed(IFileBuffer buffer) {
	}
}
