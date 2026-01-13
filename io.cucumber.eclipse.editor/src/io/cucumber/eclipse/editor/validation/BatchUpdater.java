package io.cucumber.eclipse.editor.validation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;

public abstract class BatchUpdater implements AutoCloseable {

	int references;
	Set<IDocument> documents = new LinkedHashSet<>();
	Set<IResource> resources = new LinkedHashSet<>();
	private final BatchVerificationJob previousJob;

	BatchUpdater(BatchVerificationJob previousJob) {
		this.previousJob = previousJob;
	}

	void checkForCancel() {
		if (previousJob == null) {
			return;
		}
		BatchUpdater updater = previousJob.getUpdater();
		if (documents.containsAll(updater.documents) && resources.containsAll(updater.resources)) {
			// We have already everything here (and maybe more) to update so we can cancel
			// the previous job
			previousJob.cancel();
		}
	}

	void join(IProgressMonitor monitor) throws InterruptedException {
		if (previousJob == null) {
			return;
		}
		previousJob.join(0, monitor);
	}

	@Override
	public abstract void close();

}