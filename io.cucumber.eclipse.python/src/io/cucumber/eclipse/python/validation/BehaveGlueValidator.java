package io.cucumber.eclipse.python.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;

/**
 * Performs validation on Python/Behave feature files by running behave --dry-run
 * to verify step definition matching
 * 
 * @author copilot
 */
public class BehaveGlueValidator implements IDocumentSetupParticipant {

	private static ConcurrentMap<IDocument, BehaveGlueJob> jobMap = new ConcurrentHashMap<>();

	static {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new IFileBufferListener() {

			@Override
			public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
			}

			@Override
			public void underlyingFileDeleted(IFileBuffer buffer) {
			}

			@Override
			public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
			}

			@Override
			public void stateChanging(IFileBuffer buffer) {
			}

			@Override
			public void stateChangeFailed(IFileBuffer buffer) {
			}

			@Override
			public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
			}

			@Override
			public void bufferDisposed(IFileBuffer buffer) {
				if (buffer instanceof ITextFileBuffer) {
					IDocument document = ((ITextFileBuffer) buffer).getDocument();
					BehaveGlueJob remove = jobMap.remove(document);
					if (remove != null) {
						remove.cancel();
					}
				}
			}

			@Override
			public void bufferCreated(IFileBuffer buffer) {
			}

			@Override
			public void bufferContentReplaced(IFileBuffer buffer) {
			}

			@Override
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
			}
		});
	}

	@Override
	public void setup(IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				validate(document, 1000);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
		});
		validate(document, 0);
	}

	private static void validate(IDocument document, int delay) {
		jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null) {
				oldJob.cancel();
			}
			BehaveGlueJob verificationJob = new BehaveGlueJob(() -> GherkinEditorDocument.get(document));
			verificationJob.setUser(false);
			verificationJob.setPriority(org.eclipse.core.runtime.jobs.Job.DECORATE);
			if (delay > 0) {
				verificationJob.schedule(delay);
			} else {
				verificationJob.schedule();
			}
			return verificationJob;
		});
	}

	/**
	 * Get the matched steps for a document
	 */
	public static Collection<StepMatch> getMatchedSteps(IDocument document) {
		if (document != null) {
			BehaveGlueJob job = jobMap.get(document);
			if (job != null) {
				return job.getMatchedSteps();
			}
		}
		return Collections.emptyList();
	}
}
