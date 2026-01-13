package io.cucumber.eclipse.editor.document;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
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
	private static final ConcurrentHashMap<IFile, TrackedResourceDocument> TRACKED_RESOURCES = new ConcurrentHashMap<>();
	private static IResourceChangeListener resourceChangeListener;

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
		return get(resource, false);
	}

	/**
	 * Returns a GherkinEditorDocument for the given resource.
	 * <p>
	 * If the resource is currently managed by TextFileBufferManager, returns the
	 * cached instance. Otherwise, creates a detached copy by reading the file
	 * contents. Only works with IFile resources.
	 * </p>
	 * <p>
	 * When tracking is enabled, the document is monitored for resource changes. A
	 * resource change listener is set up on first tracked request to watch for
	 * modifications, moves, and deletions. Tracked documents maintain a weak
	 * reference allowing reverse lookup from document to resource via
	 * {@link #resourceForDocument(IDocument)}. Lifecycle events are triggered:
	 * <ul>
	 * <li>documentCreated on initial tracking</li>
	 * <li>documentChanged when resource content changes</li>
	 * <li>documentDisposed when resource is deleted</li>
	 * </ul>
	 * </p>
	 * 
	 * @param resource the resource to get a GherkinEditorDocument for
	 * @param track    if true, enables resource change tracking and lifecycle
	 *                 events
	 * @return the GherkinEditorDocument for the given resource, or null if not an
	 *         IFile or if an error occurs
	 */
	public static GherkinEditorDocument get(IResource resource, boolean track) {
		if (resource instanceof IFile file) {
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(),
					LocationKind.IFILE);
			if (buffer != null) {
				return get(buffer.getDocument());
			}
			if (track) {
				ensureResourceListenerInstalled();
				TrackedResourceDocument trackedDocument = TRACKED_RESOURCES.computeIfAbsent(file,
						TrackedResourceDocument::new);
				return trackedDocument.getDocument(true);
			} else {
				return FileBasedDocument.loadFromFile(file);
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
			ITextFileBuffer buffer = getTextBuffer(document);
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
	 * Returns the text file buffer for the given document, if it exists.
	 * 
	 * @param document the document to look up
	 * @return the text file buffer, or null if the document is not managed by a buffer
	 */
	public static ITextFileBuffer getTextBuffer(IDocument document) {
		return FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
	}

	/**
	 * Resolves the workspace resource for the given document.
	 * 
	 * @param document the document to resolve
	 * @return the associated IResource, or null if it cannot be determined
	 */
	public static IResource resourceForDocument(IDocument document) {
		if (document instanceof FileBasedDocument rb) {
			return rb.getFile();
		}
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
		fireDocumentCreated(document);
	}

	/**
	 * Called when a tracked resource document is loaded for the first time.
	 * Package-private to restrict access to the document package.
	 * 
	 * @param document the document that was loaded
	 */
	static void documentLoaded(IDocument document) {
		fireDocumentCreated(document);
	}

	private static void fireDocumentCreated(IDocument document) {
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
		fireDocumentChanged(document);
	}

	private static void fireDocumentChanged(IDocument document) {
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
			fireDocumentRemoved(document);
		}
	}

	private static void fireDocumentRemoved(IDocument document) {
		for (IGherkinDocumentListener listener : LISTENERS) {
			try {
				listener.documentDisposed(document);
			} catch (Exception e) {
				// Prevent one listener from breaking others
			}
		}
	}

	/**
	 * Ensures the resource change listener is installed. Thread-safe singleton
	 * initialization.
	 * <p>
	 * The listener monitors workspace resource changes (POST_CHANGE events) to
	 * detect modifications and deletions of tracked feature files. When changes
	 * occur, the corresponding documents are reloaded and validation is triggered.
	 * </p>
	 */
	private static synchronized void ensureResourceListenerInstalled() {
		if (resourceChangeListener == null) {
			resourceChangeListener = new IResourceChangeListener() {
				@Override
				public void resourceChanged(IResourceChangeEvent event) {
					if (event.getDelta() != null) {
						try {
							event.getDelta().accept(new IResourceDeltaVisitor() {
								@Override
								public boolean visit(IResourceDelta delta) throws CoreException {
									IResource resource = delta.getResource();
									if (resource instanceof IFile file) {
										switch (delta.getKind()) {
										case IResourceDelta.REMOVED:
											handleResourceRemoved(TRACKED_RESOURCES.remove(file));
											break;
										case IResourceDelta.CHANGED:
											if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
												handleResourceChanged(TRACKED_RESOURCES.get(file));
											}
											break;
										}
									}
									return true;
								}
							});
						} catch (CoreException e) {
							// Ignore visitor errors
						}
					}
				}
			};
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener,
					IResourceChangeEvent.POST_CHANGE);
		}
	}

	/**
	 * Handles a tracked resource being removed/deleted.
	 * <p>
	 * Checks if the resource is currently mapped to a text buffer (open in editor).
	 * If not, fires document removal event. This check is not atomic but provides
	 * best-effort prevention of duplicate events.
	 * </p>
	 * 
	 * @param removed the tracked resource document that was removed
	 */
	private static void handleResourceRemoved(TrackedResourceDocument removed) {
		if (removed == null) {
			return;
		}
		if (isMappedToBuffer(removed.getFile())) {
			return;
		}
		GherkinEditorDocument document = removed.getDocument(false);
		if (document == null) {
			fireDocumentRemoved(new FileBasedDocument(removed.getFile()));
		} else {
			fireDocumentRemoved(document.getDocument());
		}
	}

	/**
	 * Handles a tracked resource being changed. Reloads the document from the
	 * resource and triggers change event.
	 * <p>
	 * Checks if the resource is currently mapped to a text buffer (open in editor).
	 * If not, reloads the document from file and fires change event. This check is
	 * not atomic but provides best-effort prevention of duplicate events.
	 * </p>
	 * 
	 * @param changed the tracked resource document that changed
	 */
	private static void handleResourceChanged(TrackedResourceDocument changed) {
		if (changed == null) {
			// not tracked at all
			return;
		}
		if (isMappedToBuffer(changed.getFile())) {
			return;
		}
		GherkinEditorDocument document = changed.reloadDocument();
		if (document == null) {
			fireDocumentRemoved(new FileBasedDocument(changed.getFile()));
		} else {
			fireDocumentChanged(document.getDocument());
		}
	}

	/**
	 * Checks if the given file is currently mapped to a text file buffer.
	 * Used to avoid duplicate event handling when a file is both open in an
	 * editor and tracked as a resource.
	 * 
	 * @param file the file to check
	 * @return true if the file has an active text buffer, false otherwise
	 */
	private static boolean isMappedToBuffer(IFile file) {
		return FileBuffers.getTextFileBufferManager()
				.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE) != null;
	}
}
