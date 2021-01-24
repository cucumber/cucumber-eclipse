package io.cucumber.eclipse.editor.validation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.messages.Messages.ParseError;

/**
 * Performs syntax-validation of the document on each change updating the
 * markers accordingly
 * 
 * @author christoph
 *
 */
public class GherkinSyntaxValidator implements IDocumentSetupParticipant {

	private static ConcurrentMap<IDocument, VerificationJob> jobMap = new ConcurrentHashMap<>();

	@Override
	public void setup(IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				// TODO configurable: delay, enable/disable validation on document change
				validate(event.getDocument(), 500, false);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
		});
		validate(document, 0, false);
		// TODO get notified on save and then validate persistently
	}

	public static Job validate(IDocument document) {
		return validate(document, 0, true);
	}

	private static Job validate(IDocument document, int delay, boolean peristent) {

		return jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null && !oldJob.peristent) {
				oldJob.cancel();
			}
			VerificationJob verificationJob = new VerificationJob(oldJob, document, peristent);
			verificationJob.setUser(false);
			verificationJob.setPriority(Job.DECORATE);
			if (delay > 0) {
				verificationJob.schedule(delay);
			} else {
				verificationJob.schedule();
			}
			return verificationJob;
		});

	}

	private static final class VerificationJob extends Job {

		private VerificationJob oldJob;
		private IDocument document;
		private boolean peristent;

		public VerificationJob(VerificationJob oldJob, IDocument document, boolean peristent) {
			super("Verify Gherkin Document");
			this.oldJob = oldJob;
			this.document = document;
			this.peristent = peristent;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (oldJob != null) {
				try {
					oldJob.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}
			}
			GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
			if (editorDocument != null) {
				IResource resource = editorDocument.getResource();
				if (resource != null) {
					List<ParseError> list = editorDocument.getParseError().collect(Collectors.toList());
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					MarkerFactory.syntaxErrorOnGherkin(resource, list, peristent);
				}
			}
			jobMap.remove(document, this);
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

}
