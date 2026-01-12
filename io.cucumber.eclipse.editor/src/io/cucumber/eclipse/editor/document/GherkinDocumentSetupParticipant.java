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
 * Integrates Gherkin documents with Eclipse's file buffer lifecycle.
 * <p>
 * This participant is registered via the {@code org.eclipse.core.filebuffers.documentSetup}
 * extension point and handles document lifecycle events for Gherkin feature files:
 * <ul>
 * <li>Registers document listeners when buffers are created</li>
 * <li>Notifies {@link GherkinEditorDocumentManager} of document changes</li>
 * <li>Cleans up resources when buffers are disposed</li>
 * </ul>
 * </p>
 * <p>
 * The setup method is called early in the document lifecycle (on empty documents),
 * so actual tracking is performed via the file buffer listener interface to ensure
 * full content is available.
 * </p>
 * 
 * @author christoph
 */
public class GherkinDocumentSetupParticipant
		implements IDocumentSetupParticipant, IDocumentListener, IFileBufferListener {

	public GherkinDocumentSetupParticipant() {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(this);
	}

	/**
	 * Called when a document is initially set up.
	 * <p>
	 * This method is invoked on empty documents, so we use it only to ensure
	 * the file buffer listener is registered. Actual document tracking happens
	 * in {@link #bufferCreated(IFileBuffer)} and {@link #bufferDisposed(IFileBuffer)}.
	 * </p>
	 */
	@Override
	public void setup(IDocument document) {
		// This method is only called on the empty document and we use it to trigger our
		// registration of the buffer listener only that do the actual work in
		// bufferCreated / bufferDisposed
	}

	/**
	 * Called when a document's content changes.
	 * <p>
	 * Notifies the document manager to invalidate the cached parsed representation
	 * and trigger change notifications to registered listeners.
	 * </p>
	 */
	@Override
	public void documentChanged(DocumentEvent event) {
		GherkinEditorDocumentManager.textBufferChanged(event.getDocument());
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	/**
	 * Called when a file buffer is created.
	 * <p>
	 * For compatible Gherkin documents, this method:
	 * <ul>
	 * <li>Registers this instance as a document listener</li>
	 * <li>Notifies the document manager of document creation</li>
	 * </ul>
	 * </p>
	 */
	@Override
	public void bufferCreated(IFileBuffer buffer) {
		if (buffer instanceof ITextFileBuffer textbuffer) {
			IDocument document = textbuffer.getDocument();
			if (GherkinEditorDocumentManager.isCompatibleTextBuffer(document)) {
				document.addDocumentListener(this);
				GherkinEditorDocumentManager.textBufferCreated(document);
			}
		}

	}

	/**
	 * Called when a file buffer is disposed.
	 * <p>
	 * Removes document listeners and notifies the document manager
	 * to clean up cached resources for the document.
	 * </p>
	 */
	@Override
	public void bufferDisposed(IFileBuffer buffer) {
		if (buffer instanceof ITextFileBuffer textbuffer) {
			IDocument document = textbuffer.getDocument();
			document.removeDocumentListener(this);
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
