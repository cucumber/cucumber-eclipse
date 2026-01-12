package io.cucumber.eclipse.editor.document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Manages cached instances of {@link GherkinEditorDocument}.
 * <p>
 * This class maintains a cache of parsed Gherkin documents and provides factory
 * methods to obtain instances for Eclipse documents and resources. Cached
 * instances are automatically invalidated when the underlying document changes.
 * </p>
 * <p>
 * The manager also handles document lifecycle events:
 * <ul>
 * <li>Tracks document creation via {@link GherkinDocumentSetupParticipant}</li>
 * <li>Monitors document changes and notifies registered listeners</li>
 * <li>Cleans up cache when documents are removed (file buffers disposed)</li>
 * <li>Prevents memory leaks by removing stale document references</li>
 * </ul>
 * </p>
 * 
 * @author christoph
 */
public final class GherkinEditorDocumentManager {

	private static final ConcurrentHashMap<IDocument, GherkinEditorDocument> DOCUMENT_MAP = new ConcurrentHashMap<>();
	private static final List<IGherkinDocumentListener> LISTENERS = new CopyOnWriteArrayList<>();

	private GherkinEditorDocumentManager() {
		// Utility class
	}

	/**
	 * Returns a cached GherkinEditorDocument for the given document. The cached
	 * instance is automatically re-parsed if the document has changed. Only works
	 * with compatible Gherkin feature documents.
	 * 
	 * @param document the document to get the corresponding GherkinEditorDocument
	 *                 for
	 * @return the GherkinEditorDocument for the given document, or null if not
	 *         compatible
	 */
	public static GherkinEditorDocument get(IDocument document) {
		return get(document, false);
	}

	/**
	 * Returns a cached or newly created GherkinEditorDocument for the given
	 * document.
	 * 
	 * @param document the document to get the corresponding GherkinEditorDocument
	 *                 for
	 * @param create   if true, creates a document even if not compatible; if false,
	 *                 returns null for incompatible documents
	 * @return the GherkinEditorDocument for the given document, or null if not
	 *         compatible and create is false
	 */
	public static GherkinEditorDocument get(IDocument document, boolean create) {
		Objects.requireNonNull(document, "document can't be null");
		if (isCompatibleTextBuffer(document)) {
			return DOCUMENT_MAP.computeIfAbsent(document, key -> {
				return GherkinEditorDocument.create(key, () -> resourceForDocument(key));
			});
		}
		if (create) {
			return GherkinEditorDocument.create(document, () -> null);
		}
		return null;
	}

	/**
	 * Returns a GherkinEditorDocument for the given resource.
	 * <p>
	 * If the resource is currently managed by TextFileBufferManager, returns the
	 * cached instance. Otherwise, creates a detached copy by reading the file
	 * contents. Only works with IFile resources.
	 * </p>
	 * 
	 * @param resource the resource to get a GherkinEditorDocument for
	 * @return the GherkinEditorDocument for the given resource, or null if not an
	 *         IFile or if an error occurs
	 */
	public static GherkinEditorDocument get(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(),
					LocationKind.IFILE);
			if (buffer != null) {
				return get(buffer.getDocument());
			}
			try {
				try (InputStream stream = file.getContents()) {
					return GherkinEditorDocument.create(new Document(IOUtils.toString(stream, file.getCharset())),
							() -> file);
				}
			} catch (IOException e) {
				return null;
			} catch (CoreException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Checks if the given document is compatible with GherkinEditorDocument. A
	 * document is compatible if it has the Gherkin feature content type.
	 * 
	 * @param document the document to check
	 * @return true if the document can be used with GherkinEditorDocument, false
	 *         otherwise
	 */
	public static boolean isCompatibleTextBuffer(IDocument document) {
		if (document != null) {
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
			if (buffer != null) {
				try {
					IContentType contentType = buffer.getContentType();
					if (contentType != null) {
						return "io.cucumber.eclipse.editor.content-type.feature".equals(contentType.getId());
					}
				} catch (CoreException e) {
				}
			}
		}
		return false;
	}

	/**
	 * Resolves the workspace resource for the given document.
	 * 
	 * @param document the document to resolve
	 * @return the associated IResource, or null if it cannot be determined
	 */
	public static IResource resourceForDocument(IDocument document) {
		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
		if (buffer != null) {
			IPath location = buffer.getLocation();
			if (location != null) {
				IFile res = ResourcesPlugin.getWorkspace().getRoot().getFile(location);
				if (res != null && res.exists()) {
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * Registers a listener to receive document lifecycle events.
	 * <p>
	 * Listeners will be notified when documents are created, changed, or removed.
	 * Thread-safe and uses copy-on-write semantics.
	 * </p>
	 * 
	 * @param listener the listener to register
	 */
	public static void addDocumentListener(IGherkinDocumentListener listener) {
		if (listener != null && !LISTENERS.contains(listener)) {
			LISTENERS.add(listener);
		}
	}

	/**
	 * Unregisters a document lifecycle listener.
	 * 
	 * @param listener the listener to remove
	 */
	public static void removeDocumentListener(IGherkinDocumentListener listener) {
		LISTENERS.remove(listener);
	}

	/**
	 * Called by {@link GherkinDocumentSetupParticipant} when a document is set up.
	 * Package-private to restrict access to the document package.
	 * 
	 * @param document the document that was set up
	 */
	static void textBufferCreated(IDocument document) {
		for (IGherkinDocumentListener listener : LISTENERS) {
			try {
				listener.documentCreated(document);
			} catch (Exception e) {
				// Prevent one listener from breaking others
			}
		}
	}

	/**
	 * Called by {@link GherkinDocumentSetupParticipant} when a document changes.
	 * Package-private to restrict access to the document package.
	 * 
	 * @param document the document that changed
	 */
	static void textBufferChanged(IDocument document) {
		DOCUMENT_MAP.remove(document);
		for (IGherkinDocumentListener listener : LISTENERS) {
			try {
				listener.documentChanged(document);
			} catch (Exception e) {
				// Prevent one listener from breaking others
			}
		}
	}

	/**
	 * Notifies listeners that a document has been removed.
	 * 
	 * @param document the document that was removed
	 */
	static void textBufferRemoved(IDocument document) {
		GherkinEditorDocument removed = DOCUMENT_MAP.remove(document);
		if (removed != null) {
			for (IGherkinDocumentListener listener : LISTENERS) {
				try {
					listener.documentDisposed(document);
				} catch (Exception e) {
					// Prevent one listener from breaking others
				}
			}
		}
	}
}
