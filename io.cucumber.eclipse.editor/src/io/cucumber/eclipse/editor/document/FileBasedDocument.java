package io.cucumber.eclipse.editor.document;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextStore;

/**
 * A thread-safe document implementation backed by a file resource.
 * <p>
 * This document extends {@link Document} and adds synchronization to all
 * critical operations to ensure thread safety when accessed from multiple
 * threads (e.g., UI thread and validation jobs). The document can be reloaded
 * from its underlying file resource, making it suitable for tracking file
 * changes outside of an editor.
 * </p>
 * <p>
 * Instances are created and managed by {@link GherkinEditorDocumentManager}
 * for tracked resources that are not currently open in an editor.
 * </p>
 * 
 * @see TrackedResourceDocument
 * @see GherkinEditorDocumentManager#get(IResource, boolean)
 */
class FileBasedDocument extends Document {

	private IFile file;

	/**
	 * Creates a new file-based document for the given file resource.
	 * The document content is not loaded until explicitly requested.
	 * 
	 * @param resource the file resource backing this document
	 */
	FileBasedDocument(IFile resource) {
		super();
		this.file = resource;
	}

	private FileBasedDocument(IFile file, String content) {
		super(content);
		this.file = file;
	}

	/**
	 * Returns the file resource backing this document.
	 * 
	 * @return the file resource
	 */
	IResource getFile() {
		return file;
	}

	@Override
	public synchronized String get() {
		return super.get();
	}

	@Override
	public synchronized void set(String text) {
		super.set(text);
	}

	@Override
	protected synchronized ITextStore getStore() {
		return super.getStore();
	}

	@Override
	protected synchronized void setTextStore(ITextStore store) {
		super.setTextStore(store);
	}

	/**
	 * Reloads the document content from the underlying file resource.
	 * This method is synchronized to prevent concurrent modification during reload.
	 * 
	 * @throws IOException if reading the file fails
	 * @throws CoreException if accessing the file resource fails
	 */
	void reload() throws IOException, CoreException {
		try (InputStream stream = file.getContents()) {
			set(IOUtils.toString(stream, file.getCharset()));
		}
	}

	/**
	 * Creates a new GherkinEditorDocument by loading content from the specified file.
	 * 
	 * @param file the file to load
	 * @return a new GherkinEditorDocument, or null if loading fails
	 */
	static GherkinEditorDocument loadFromFile(IFile file) {
		try {
			try (InputStream stream = file.getContents()) {
				FileBasedDocument document = new FileBasedDocument(file,
						IOUtils.toString(stream, file.getCharset()));
				return GherkinEditorDocument.create(document, () -> file);
			}
		} catch (IOException | CoreException e) {
			return null;
		}
	}

}
