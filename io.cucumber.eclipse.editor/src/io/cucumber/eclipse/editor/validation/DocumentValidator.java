package io.cucumber.eclipse.editor.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;

/**
 * Central document validator for Gherkin feature files.
 * <p>
 * This class implements {@link IDocumentSetupParticipant} to automatically
 * attach validation to feature file documents when they are opened. It
 * coordinates both syntax validation and glue code validation in a unified way.
 * </p>
 * <p>
 * The validator handles:
 * <ul>
 * <li>Document lifecycle tracking (setup and disposal)</li>
 * <li>Debounced validation on document changes</li>
 * <li>Background job management</li>
 * <li>Cleanup when documents are closed</li>
 * </ul>
 * </p>
 * <p>
 * Validation is performed in two stages by {@link VerificationJob}:
 * <ol>
 * <li>Syntax validation to detect Gherkin parse errors</li>
 * <li>Glue validation (only if syntax is valid) to match steps with
 * definitions</li>
 * </ol>
 * </p>
 * 
 * @see VerificationJob
 * @see IGlueValidator
 */
public class DocumentValidator implements IDocumentSetupParticipant {

	/**
	 * Maps documents to their currently running or scheduled validation jobs.
	 * Thread-safe to allow concurrent access from UI and background threads.
	 */
	private static ConcurrentMap<IDocument, VerificationJob> jobMap = new ConcurrentHashMap<>();

	static {
		/*
		 * Listens for file buffer lifecycle events to clean up validation jobs when
		 * documents are closed. This prevents memory leaks and unnecessary validation
		 * of closed documents.
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
					VerificationJob job = jobMap.remove(document);
					if (job != null) {
						job.cancel();
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
					timeout = io.cucumber.eclipse.editor.preferences.CucumberEditorPreferences.of(editorDoc.getResource()).getValidationTimeout();
				} else {
					timeout = io.cucumber.eclipse.editor.preferences.CucumberEditorPreferences.DEFAULT_VALIDATION_TIMEOUT;
				}
				validate(event.getDocument(), timeout);
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
	 * This method cancels any existing validation job for the document and
	 * schedules a new one. The delay parameter allows for debouncing to avoid
	 * excessive validation during rapid typing.
	 * </p>
	 * 
	 * @param document the document to validate
	 * @param delay    the delay in milliseconds before validation starts (0 for
	 *                 immediate)
	 */
	private static void validate(IDocument document, int delay) {
		jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null) {
				oldJob.cancel();
			}
			GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
			VerificationJob verificationJob = new VerificationJob(oldJob, editorDocument);
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
	 * Triggers revalidation for all currently tracked documents.
	 * <p>
	 * This method is useful when global configuration changes affect all feature
	 * files, such as preference updates or glue code changes.
	 * </p>
	 */
	public static void revalidateAllDocuments() {
		Map<IDocument, VerificationJob> snapshot = new HashMap<>(jobMap);
		snapshot.keySet().forEach(document -> {
			IResource resource = GherkinEditorDocument.resourceForDocument(document);
			if (resource == null) {
				// Document (no longer) managed by a text buffer...
				return;
			}
			int timeout = io.cucumber.eclipse.editor.preferences.CucumberEditorPreferences.of(resource)
					.getValidationTimeout();
			validate(document, timeout);
		});
	}

	/**
	 * Cleans up all validation jobs and resources.
	 * <p>
	 * This method should be called when the plugin is stopping to ensure proper
	 * cleanup of all running jobs.
	 * </p>
	 */
	public static void shutdown() {
		jobMap.values().forEach(Job::cancel);
		jobMap.clear();
	}

}
