package io.cucumber.eclipse.editor.document;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.text.IDocument;

/**
 * Holds a tracked resource and a weak reference to its document.
 * <p>
 * This class manages the lifecycle of a {@link GherkinEditorDocument} for a
 * file resource that is being tracked for validation but may not be currently
 * open in an editor. It uses a {@link WeakReference} to allow the document to
 * be garbage collected when no longer actively used, while still maintaining
 * the ability to reload it from the file when needed.
 * </p>
 * <p>
 * The weak reference approach provides several benefits:
 * <ul>
 * <li>Memory efficiency: Documents can be garbage collected when not in use</li>
 * <li>On-demand loading: Documents are loaded only when needed</li>
 * <li>Reverse lookup: Allows finding the resource for a document even after
 * the document is no longer strongly referenced elsewhere</li>
 * </ul>
 * </p>
 * <p>
 * Thread-safe for concurrent access to document retrieval and reloading.
 * </p>
 * 
 * @see GherkinEditorDocumentManager#get(IResource, boolean)
 */
final class TrackedResourceDocument {
	private final IFile file;
	private WeakReference<GherkinEditorDocument> documentRef;

	TrackedResourceDocument(IFile resource) {
		this.file = resource;
	}

	/**
	 * Returns the document for this tracked resource.
	 * 
	 * @param load if true, loads the document from file if not currently in memory;
	 *             if false, returns null if document is not in memory
	 * @return the GherkinEditorDocument, or null if not available or loading failed
	 */
	public synchronized GherkinEditorDocument getDocument(boolean load) {
		GherkinEditorDocument document = documentRef == null ? null : documentRef.get();
		if (load && document == null) {
			return loadDocument();
		}
		return document;
	}

	/**
	 * Reloads the document content from the file resource.
	 * <p>
	 * If the document is a {@link FileBasedDocument}, attempts to reload its
	 * content from the file. If reloading fails or the document is not file-based,
	 * falls back to loading a fresh document.
	 * </p>
	 * 
	 * @return the reloaded GherkinEditorDocument, or null if loading failed
	 */
	public synchronized GherkinEditorDocument reloadDocument() {
		if (documentRef != null) {
			GherkinEditorDocument editorDocument = documentRef.get();
			if (editorDocument != null) {
				IDocument document = editorDocument.getDocument();
				if (document instanceof FileBasedDocument resourceDocument) {
					try {
						resourceDocument.reload();
						GherkinEditorDocument reloaded = new GherkinEditorDocument(document, () -> file);
						documentRef = new WeakReference<>(reloaded);
						return reloaded;
					} catch (IOException | CoreException e) {
						ILog.get().warn("Failed to reload document for " + file.getFullPath(), e);
					}
				}
			}
		}
		return loadDocument();
	}

	private synchronized GherkinEditorDocument loadDocument() {
		GherkinEditorDocument editorDocument = FileBasedDocument.loadFromFile(file);
		if (editorDocument != null) {
			boolean isInitialLoad = documentRef == null;
			documentRef = new WeakReference<>(editorDocument);
			if (isInitialLoad) {
				GherkinEditorDocumentManager.documentLoaded(editorDocument.getDocument());
			}
		}
		return editorDocument;
	}

	/**
	 * Returns the file resource being tracked.
	 * 
	 * @return the file resource
	 */
	public IFile getFile() {
		return file;
	}

}