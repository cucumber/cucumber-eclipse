package io.cucumber.eclipse.editor.validation;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;

/**
 * Verification job for text buffer documents.
 * <p>
 * This job validates Gherkin documents that are currently open in an editor
 * and managed by a text file buffer. The document is retrieved from
 * {@link GherkinEditorDocumentManager} using the IDocument instance.
 * </p>
 * 
 * @see ResourceVerificationJob
 */
class TextBufferVerificationJob extends VerificationJob {

	private IDocument document;

	/**
	 * Creates a new verification job for a text buffer document.
	 * 
	 * @param document the document to verify
	 * @param name the display name for the job
	 */
	TextBufferVerificationJob(IDocument document, String name) {
		super(name);
		this.document = document;
	}

	/**
	 * Retrieves the editor documents from the manager using the IDocument instance.
	 */
	@Override
	protected Collection<GherkinEditorDocument> getEditorDocuments() {
		GherkinEditorDocument editorDocument = GherkinEditorDocumentManager.get(document);
		return editorDocument == null ? List.of() : List.of(editorDocument);
	}

	@Override
	public boolean matches(IProject project) {
		IResource resource = GherkinEditorDocumentManager.resourceForDocument(document);
		if (resource != null) {
			return resource.getProject() == project;
		}
		return false;
	}

}
