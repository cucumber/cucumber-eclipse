package io.cucumber.eclipse.java.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;

/**
 * Performs a dry-run on the document to verify step definition matching
 * 
 * @author christoph
 *
 */
public class CucumberGlueValidator implements IDocumentSetupParticipant {

	private static ConcurrentMap<IDocument, GlueJob> jobMap = new ConcurrentHashMap<>();

	static {
		// TODO implement generic DocumentCache class
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
				System.out.println("dirtyStateChanged: " + buffer.getLocation() + " dirty = " + isDirty);

			}

			@Override
			public void bufferDisposed(IFileBuffer buffer) {
				if (buffer instanceof ITextFileBuffer) {
					IDocument document = ((ITextFileBuffer) buffer).getDocument();
					GlueJob remove = jobMap.remove(document);
					if (remove != null) {
						remove.cancel();
						remove.disposeListener();
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

	public static void revalidate(IDocument document) {
		validate(document, 0);
	}

	@Override
	public void setup(IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				// TODO configurable
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
				oldJob.disposeListener();
			}
			GlueJob verificationJob = new GlueJob(oldJob, document);
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

	/**
	 * Allows to sync with the current glue code computation
	 * 
	 * @param document the document to sync on
	 * @param monitor  the progress monitor that can be used to cancel the join
	 *                 operation, or null if cancellation is not required. No
	 *                 progress is reported on this monitor.
	 * @throws OperationCanceledException on cancellation
	 * @throws InterruptedException       if the thread was interrupted while
	 *                                    waiting
	 */
	private static GlueJob sync(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		GlueJob glueJob = jobMap.get(document);
		if (glueJob != null) {
			glueJob.join(TimeUnit.SECONDS.toMillis(30), monitor);
		}
		return glueJob;
	}

	public static Collection<MatchedStep<?>> getMatchedSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
//TODO		Objects.requireNonNull(document);
		if (document != null) {
			GlueJob job = sync(document, monitor);
			if (job != null) {
				return job.matchedSteps;
			}
		}
		return Collections.emptyList();
	}

	public static Collection<CucumberStepDefinition> getAvaiableSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		// TODO Objects.requireNonNull(document);
		if (document != null) {
			GlueJob job = sync(document, monitor);
			if (job != null) {
				return job.parsedSteps;
			}
		}
		return Collections.emptyList();
	}

}
