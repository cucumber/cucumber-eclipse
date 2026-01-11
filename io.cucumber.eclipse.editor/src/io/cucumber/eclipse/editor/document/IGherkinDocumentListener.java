package io.cucumber.eclipse.editor.document;

import org.eclipse.jface.text.IDocument;

/**
 * Listener interface for Gherkin document lifecycle events.
 * <p>
 * Implementations of this interface can be registered with the
 * {@link GherkinEditorDocumentManager} to receive notifications about
 * document creation, changes, and removal.
 * </p>
 * <p>
 * This is useful for components that need to react to document lifecycle
 * events, such as validators that need to schedule validation jobs or
 * cleanup resources when documents are removed.
 * </p>
 * 
 * @author christoph
 */
public interface IGherkinDocumentListener {

	/**
	 * Called when a new Gherkin document is set up.
	 * <p>
	 * This is invoked when a feature file document is first created
	 * and registered with the manager.
	 * </p>
	 * 
	 * @param document the document that was set up
	 * @param editorDocument the parsed Gherkin editor document
	 */
	void documentSetup(IDocument document);

	/**
	 * Called when a Gherkin document has changed.
	 * <p>
	 * This is invoked after the document content has been modified.
	 * Listeners can use this to schedule validation or other processing.
	 * </p>
	 * 
	 * @param document the document that changed
	 * @param editorDocument the parsed Gherkin editor document (may be stale if dirty)
	 */
	void documentChanged(IDocument document);

	/**
	 * Called when a Gherkin document is removed from the manager.
	 * <p>
	 * This is invoked when the document's file buffer is disposed,
	 * typically when the editor is closed. Listeners should cleanup
	 * any resources associated with the document.
	 * </p>
	 * 
	 * @param document the document that was removed
	 */
	void documentRemoved(IDocument document);
}
