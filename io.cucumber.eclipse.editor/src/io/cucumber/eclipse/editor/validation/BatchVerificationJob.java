package io.cucumber.eclipse.editor.validation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;

class BatchVerificationJob extends VerificationJob {

	private BatchUpdater updater;

	BatchVerificationJob(BatchUpdater updater) {
		super("Features");
		this.updater = updater;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.subTask("Wait for previous jobs to complete...");
		try {
			// join other verification jobs first ...
			Job.getJobManager().join(VerificationJob.class, monitor);
		} catch (InterruptedException | OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
		try {
			// Join any maybe preceding batch updater
			updater.join(monitor);
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return super.run(monitor);
	}

	@Override
	public boolean belongsTo(Object family) {
		return family == BatchVerificationJob.class || family == IGlueValidator.class;
	}

	@Override
	protected Collection<GherkinEditorDocument> getEditorDocuments() {
		Map<IResource, GherkinEditorDocument> collected = new LinkedHashMap<>();
		for (IDocument doc : updater.documents) {
			GherkinEditorDocument editorDocument = GherkinEditorDocumentManager.get(doc);
			if (editorDocument != null) {
				IResource resource = editorDocument.getResource();
				if (resource != null) {
					collected.put(resource, editorDocument);
				}
			}
		}
		for (IResource resource : updater.resources) {
			if (collected.containsKey(resource)) {
				continue;
			}
			GherkinEditorDocument editorDocument = GherkinEditorDocumentManager.get(resource);
			if (editorDocument != null) {
				collected.put(resource, editorDocument);
			}
		}
		return collected.values();
	}

	public BatchUpdater getUpdater() {
		return updater;
	}

	@Override
	public boolean matches(IProject project) {
		for (IDocument doc : updater.documents) {
			IResource resource = GherkinEditorDocumentManager.resourceForDocument(doc);
			if (resource != null && resource.getProject() == project) {
				return true;
			}
		}
		for (IResource resource : updater.resources) {
			if (resource.getProject() == project) {
				return true;
			}
		}
		return false;
	}

}
