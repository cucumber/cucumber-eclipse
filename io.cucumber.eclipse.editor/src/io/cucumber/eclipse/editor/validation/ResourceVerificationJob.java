package io.cucumber.eclipse.editor.validation;

import org.eclipse.core.resources.IResource;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;

/**
 * Verification job for resource-tracked documents.
 * <p>
 * This job validates Gherkin documents that are tracked as resources but not
 * currently open in an editor. The document is retrieved from
 * {@link GherkinEditorDocumentManager} using the resource path.
 * </p>
 * 
 * @see TrackedResourceDocument
 * @see TextBufferVerificationJob
 */
class ResourceVerificationJob extends VerificationJob {

	private IResource resource;

	/**
	 * Creates a new verification job for a tracked resource.
	 * 
	 * @param resource the resource to verify
	 */
	ResourceVerificationJob(IResource resource) {
		super(resource.getFullPath().toOSString());
		this.resource = resource;
	}

	/**
	 * Retrieves the editor document from the manager using the resource.
	 */
	@Override
	protected GherkinEditorDocument getEditorDocument() {
		return GherkinEditorDocumentManager.get(resource);
	}

}
