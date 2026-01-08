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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferences;

/**
 * Validates Cucumber feature files by matching Gherkin steps with their Java step definitions.
 * <p>
 * This validator performs background validation of feature files to identify unmatched steps
 * and provide navigation support to step definitions. It implements {@link IDocumentSetupParticipant}
 * to automatically attach validation to feature file documents when they are opened in Eclipse.
 * </p>
 * <p>
 * The validation process involves:
 * <ul>
 * <li>Running Cucumber in dry-run mode to discover available step definitions</li>
 * <li>Matching Gherkin steps from feature files with discovered step definitions</li>
 * <li>Creating markers for unmatched steps to alert developers</li>
 * <li>Maintaining a cache of matched steps for navigation (Ctrl+Click) support</li>
 * </ul>
 * </p>
 * <p>
 * Validation is triggered automatically on document changes with a configurable delay to avoid
 * excessive validation during typing. The validator manages background jobs to perform validation
 * without blocking the UI thread.
 * </p>
 * 
 * @see GlueJob for the background validation implementation
 * @see CucumberStepDefinition for step definition representation
 * @see MatchedStep for matched step information
 */
public class CucumberGlueValidator implements IDocumentSetupParticipant {

	/**
	 * Maps documents to their currently running or scheduled validation jobs.
	 * Thread-safe to allow concurrent access from UI and background threads.
	 */
	private static ConcurrentMap<IDocument, GlueJob> jobMap = new ConcurrentHashMap<>();

	static {
		/*
		 * Listens for file buffer lifecycle events to clean up validation jobs
		 * when documents are closed. This prevents memory leaks and unnecessary
		 * validation of closed documents.
		 */
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

	/**
	 * Triggers immediate validation of the specified document.
	 * <p>
	 * This method schedules validation without any delay, useful when an immediate
	 * update is required (e.g., after configuration changes or manual refresh).
	 * </p>
	 * 
	 * @param document the document to revalidate
	 */
	public static void revalidate(IDocument document) {
		validate(document, 0);
	}

	/**
	 * Sets up validation for a feature file document.
	 * <p>
	 * Called automatically by Eclipse when a feature file is opened. This method:
	 * <ul>
	 * <li>Attaches a document listener to track changes</li>
	 * <li>Schedules validation with a delay after each change (debouncing)</li>
	 * <li>Performs an initial validation immediately</li>
	 * </ul>
	 * </p>
	 * 
	 * @param document the document to set up validation for
	 */
	@Override
	public void setup(IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				GherkinEditorDocument editorDoc = GherkinEditorDocument.get(document);
				int timeout;
				if (editorDoc != null && editorDoc.getResource() != null) {
					timeout = CucumberJavaPreferences.of(editorDoc.getResource()).validationTimeout();
				} else {
					timeout = CucumberJavaPreferences.DEFAULT_VALIDATION_TIMEOUT;
				}
				validate(document, timeout);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {

			}
		});
		validate(document, 0);
	}


	/**
	 * Schedules validation for the specified document with an optional delay.
	 * <p>
	 * This method cancels any existing validation job for the document and schedules
	 * a new one. The delay parameter allows for debouncing to avoid excessive validation
	 * during rapid typing.
	 * </p>
	 * 
	 * @param document the document to validate
	 * @param delay the delay in milliseconds before validation starts (0 for immediate)
	 */
	private static void validate(IDocument document, int delay) {
		jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null) {
				oldJob.cancel();
				oldJob.disposeListener();
			}
			GlueJob verificationJob = new GlueJob(oldJob, () -> GherkinEditorDocument.get(document));
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
	 * Triggers validation for the specified editor document.
	 * <p>
	 * This method provides a public API for other plugins to trigger validation programmatically.
	 * Unlike the editor-based validation that only occurs when a file is opened, this method
	 * allows external validation triggers.
	 * </p>
	 * <p>
	 * The method handles temporary documents (not associated with an open editor) by automatically
	 * cleaning up the validation job when complete. For documents with open editors, the job
	 * remains cached for subsequent validations and navigation support.
	 * </p>
	 * 
	 * @param editorDocument the Gherkin editor document to validate
	 * @return the validation job that was scheduled, which can be used to:
	 *         <ul>
	 *         <li>Wait for validation to complete using {@code job.join()}</li>
	 *         <li>Cancel validation using {@code job.cancel()}</li>
	 *         <li>Track job status with job change listeners</li>
	 *         </ul>
	 */
	public static Job validate(GherkinEditorDocument editorDocument) {
		return jobMap.compute(editorDocument.getDocument(), (key, oldJob) -> {
			if (oldJob != null) {
				oldJob.cancel();
				oldJob.disposeListener();
			}
			GlueJob verificationJob = new GlueJob(oldJob, () -> editorDocument);
			verificationJob.addJobChangeListener(new IJobChangeListener() {

				@Override
				public void sleeping(IJobChangeEvent event) {
				}

				@Override
				public void scheduled(IJobChangeEvent event) {
				}

				@Override
				public void running(IJobChangeEvent event) {
				}

				@Override
				public void done(IJobChangeEvent event) {
					jobMap.compute(editorDocument.getDocument(), (key, currentJob) -> {
						if (currentJob == verificationJob
								&& GherkinEditorDocument.get(editorDocument.getDocument()) == null) {
							// this was a temporary job and there is no editor for it so we need to remove
							// the job from the map as we won'T get notifications from the text-buffer!
							return null;
						}
						return currentJob;
					});
				}

				@Override
				public void awake(IJobChangeEvent event) {

				}

				@Override
				public void aboutToRun(IJobChangeEvent event) {

				}
			});
			verificationJob.setUser(false);
			verificationJob.setPriority(Job.DECORATE);
			verificationJob.schedule();
			return verificationJob;
		});
	}

	/**
	 * Synchronizes with the current validation job for the specified document.
	 * <p>
	 * This method blocks the calling thread until the validation job completes or a timeout
	 * occurs (30 seconds). It is used internally by methods that need to access validation
	 * results synchronously, such as {@link #getMatchedSteps(IDocument, IProgressMonitor)}.
	 * </p>
	 * 
	 * @param document the document to synchronize on
	 * @param monitor the progress monitor for cancellation support, or {@code null} if
	 *                cancellation is not needed. No progress is reported to this monitor.
	 * @return the validation job that was synchronized with, or {@code null} if no job exists
	 * @throws OperationCanceledException if the operation was cancelled via the monitor
	 * @throws InterruptedException if the thread was interrupted while waiting
	 */
	private static GlueJob sync(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		GlueJob glueJob = jobMap.get(document);
		if (glueJob != null) {
			glueJob.join(TimeUnit.SECONDS.toMillis(30), monitor);
		}
		return glueJob;
	}

	/**
	 * Retrieves the matched steps for the specified document.
	 * <p>
	 * This method provides access to step matching results from the most recent validation.
	 * It waits for any ongoing validation to complete before returning the results.
	 * Each {@link MatchedStep} contains information about a Gherkin step and its corresponding
	 * step definition, enabling features like navigation (Ctrl+Click) to step definitions.
	 * </p>
	 * 
	 * @param document the document to get matched steps for
	 * @param monitor the progress monitor for cancellation support, or {@code null}
	 * @return a collection of matched steps, or an empty collection if:
	 *         <ul>
	 *         <li>The document is {@code null}</li>
	 *         <li>No validation has been performed yet</li>
	 *         <li>Validation failed or was cancelled</li>
	 *         </ul>
	 * @throws OperationCanceledException if the operation was cancelled via the monitor
	 * @throws InterruptedException if the thread was interrupted while waiting for validation
	 */
	public static Collection<MatchedStep<?>> getMatchedSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		if (document != null) {
			GlueJob job = sync(document, monitor);
			if (job != null) {
				return job.matchedSteps;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves all available step definitions discovered for the specified document.
	 * <p>
	 * This method provides access to all step definitions found during validation, regardless
	 * of whether they match any steps in the current document. It waits for any ongoing
	 * validation to complete before returning the results. The returned step definitions
	 * can be used for content assist, code completion, or analysis.
	 * </p>
	 * 
	 * @param document the document to get available step definitions for
	 * @param monitor the progress monitor for cancellation support, or {@code null}
	 * @return a collection of all discovered step definitions, or an empty collection if:
	 *         <ul>
	 *         <li>The document is {@code null}</li>
	 *         <li>No validation has been performed yet</li>
	 *         <li>Validation failed or was cancelled</li>
	 *         <li>No step definitions were found in the project</li>
	 *         </ul>
	 * @throws OperationCanceledException if the operation was cancelled via the monitor
	 * @throws InterruptedException if the thread was interrupted while waiting for validation
	 */
	public static Collection<CucumberStepDefinition> getAvailableSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		if (document != null) {
			GlueJob job = sync(document, monitor);
			if (job != null) {
				return job.parsedSteps;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Triggers validation for all feature files in the specified project.
	 * <p>
	 * This method recursively visits all resources in the project and schedules validation
	 * for each feature file found. It is useful for batch validation scenarios such as:
	 * <ul>
	 * <li>Project import or refresh</li>
	 * <li>Build clean operations</li>
	 * <li>Manual "Validate All" actions</li>
	 * <li>Configuration changes that affect step definitions</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note: This method schedules validation jobs but does not wait for them to complete.
	 * Each feature file gets its own validation job that runs in the background.
	 * </p>
	 * 
	 * @param project the Eclipse project to validate
	 * @param monitor the progress monitor for tracking and cancellation, or {@code null}
	 * @throws CoreException if resource visitation fails due to workspace issues
	 */
	public static void validateProject(IProject project, IProgressMonitor monitor) throws CoreException {
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if ("feature".equals(file.getFileExtension())) {
						validate(GherkinEditorDocument.get(file));
					}
				}
				return true;
			}
		});

	}

}
