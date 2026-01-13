package io.cucumber.eclipse.editor.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;

import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.editor.document.IGherkinDocumentListener;
import io.cucumber.eclipse.editor.preferences.CucumberEditorPreferences;

/**
 * Central document validator for Gherkin feature files.
 * <p>
 * This class implements {@link IGherkinDocumentListener} to automatically
 * receive notifications about document lifecycle events from the
 * {@link GherkinEditorDocumentManager}. It coordinates both syntax validation
 * and glue code validation in a unified way.
 * </p>
 * <p>
 * The validator handles:
 * <ul>
 * <li>Document lifecycle tracking via manager notifications</li>
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
 * <p>
 * This validator is registered as a singleton with the manager during plugin
 * activation.
 * </p>
 * 
 * @see VerificationJob
 * @see IGlueValidator
 * @see GherkinEditorDocumentManager
 */
public class DocumentValidator implements IGherkinDocumentListener {

	/**
	 * Text buffered documents are opened inside an editor and currently mapped to a
	 * text buffer. A text buffered document changes while the user edit the
	 * document but not necessarily have saved the file.
	 */
	private static final ConcurrentMap<IDocument, VerificationJob> textBufferDocuments = new ConcurrentHashMap<>();

	/**
	 * Resource based documents are currently not opened but tracked for
	 * verification
	 */
	private static final ConcurrentMap<IResource, VerificationJob> resourceDocuments = new ConcurrentHashMap<>();

	/**
	 * Singleton instance of the validator.
	 */
	private static final DocumentValidator INSTANCE = new DocumentValidator();

	private BatchUpdater batch;

	/**
	 * Private constructor - use singleton instance.
	 */
	private DocumentValidator() {
	}

	/**
	 * Initializes the validator by registering it with the document manager. Should
	 * be called during plugin activation.
	 */
	public static void initialize() {
		GherkinEditorDocumentManager.addDocumentListener(INSTANCE);
	}

	/**
	 * Called when a new Gherkin document is set up. Schedules immediate validation
	 * for the new document.
	 */
	@Override
	public void documentCreated(IDocument document) {
		validate(document, 0);
	}

	/**
	 * Called when a Gherkin document changes. Schedules validation with a
	 * configurable delay (debouncing).
	 */
	@Override
	public void documentChanged(IDocument document) {
		IResource resource = GherkinEditorDocumentManager.resourceForDocument(document);
		if (resource == null) {
			return;
		}
		int timeout = CucumberEditorPreferences.of(resource).getValidationTimeout();
		validate(document, timeout);
	}

	/**
	 * Called when a Gherkin document is removed. Cancels any running validation job
	 * and cleans up resources.
	 */
	@Override
	public void documentDisposed(IDocument document) {
		VerificationJob job = textBufferDocuments.remove(document);
		if (job != null) {
			job.cancel();
		}
		IResource resource = GherkinEditorDocumentManager.resourceForDocument(document);
		if (resource != null) {
			VerificationJob remove = resourceDocuments.remove(resource);
			if (remove != null) {
				remove.cancel();
			}
		}
	}

	/**
	 * Schedules validation for the specified document with an optional delay.
	 * <p>
	 * This method reuses the existing validation job for the document if one
	 * exists, or creates a new one. If a job is already scheduled or running, it
	 * will be canceled and rescheduled. The delay parameter allows for debouncing
	 * to avoid excessive validation during rapid typing.
	 * </p>
	 * 
	 * @param document the document to validate
	 * @param delay    the delay in milliseconds before validation starts (0 for
	 *                 immediate)
	 */
	private static void validate(IDocument document, int delay) {
		ITextFileBuffer textBuffer = GherkinEditorDocumentManager.getTextBuffer(document);
		if (textBuffer != null) {
			VerificationJob job = textBufferDocuments.computeIfAbsent(document, doc -> {
				IPath location = textBuffer.getLocation();
				return new TextBufferVerificationJob(document, location == null ? "Document" : location.toOSString());
			});
			if (INSTANCE.addToBatch(document)) {
				return;
			}
			job.schedule(Math.max(0, delay));
			return;
		}
		IResource resource = GherkinEditorDocumentManager.resourceForDocument(document);
		if (resource != null) {
			validate(resource, delay);
			return;
		}
	}

	private static void validate(IResource resource, int delay) {
		VerificationJob job = resourceDocuments.computeIfAbsent(resource, ResourceVerificationJob::new);
		if (INSTANCE.addToBatch(resource)) {
			return;
		}
		job.schedule(Math.max(0, delay));
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
		Set<IResource> scheduled = new HashSet<>();
		textBufferDocuments.forEach((doc, job) -> {
			IResource resource = GherkinEditorDocumentManager.resourceForDocument(doc);
			if (resource == null) {
				// Document (no longer) managed by a text buffer...
				return;
			}
			if (INSTANCE.addToBatch(doc)) {
				return;
			}
			scheduleValidation(resource, job);
			scheduled.add(resource);
		});
		resourceDocuments.forEach((resource, job) -> {
			if (scheduled.add(resource)) {
				if (INSTANCE.addToBatch(resource)) {
					return;
				}
				scheduleValidation(resource, job);
			}
		});
	}

	private static void scheduleValidation(IResource resource, VerificationJob job) {
		int timeout = CucumberEditorPreferences.of(resource).getValidationTimeout();
		job.schedule(Math.max(0, timeout));
	}

	/**
	 * Triggers revalidation for all documents in the specified project.
	 * <p>
	 * This method is useful when project-specific configuration changes affect
	 * feature files, such as glue path changes or project setup modifications.
	 * Ensures each resource is validated only once even if tracked both as text
	 * buffer and resource.
	 * </p>
	 * 
	 * @param project the project whose documents should be revalidated
	 */
	public static void revalidateDocuments(IProject project) {
		Set<IResource> scheduled = new HashSet<>();
		textBufferDocuments.forEach((doc, job) -> {
			IResource resource = GherkinEditorDocumentManager.resourceForDocument(doc);
			if (resource == null || resource.getProject() != project) {
				return;
			}
			if (INSTANCE.addToBatch(doc)) {
				return;
			}
			scheduleValidation(resource, job);
		});
		resourceDocuments.forEach((resource, job) -> {

			if (resource.getProject() == project && scheduled.add(resource)) {
				if (INSTANCE.addToBatch(resource)) {
					return;
				}
				scheduleValidation(resource, job);
			}
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
		textBufferDocuments.values().forEach(Job::cancel);
		textBufferDocuments.clear();
		resourceDocuments.values().forEach(Job::cancel);
		resourceDocuments.clear();
	}

	private synchronized void performBatchUpdate() {
		BatchUpdater updater = this.batch;
		if (updater == null) {
			return;
		}
		updater.references--;
		// Check if we can cancel any previous batch
		updater.checkForCancel();
		if (updater.references <= 0) {
			this.batch = null;
			if (updater.documents.isEmpty() && updater.resources.isEmpty()) {
				return;
			}
			if (updater.documents.size() + updater.resources.size() == 1) {
				for (IDocument doc : updater.documents) {
					validate(doc, 0);
				}
				for (IResource resource : updater.resources) {
					validate(resource, 0);
				}
				return;
			}
			BatchVerificationJob job = new BatchVerificationJob(updater);
			// During a batch run in the background we collect other requests for update in
			// another batch to prevent concurrent runs of verification. The most likely
			// event is that the user is editing a feature file in the meanwhile, so once
			// our batch job completes, we complete the next batch created here, what
			// results in a single document and then the cycle ends (or we trigger a new
			// batch verification if more things have been changed.
			BatchUpdater nextBatch = getBatch(job);
			job.addJobChangeListener(new IJobChangeListener() {

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
					nextBatch.close();

				}

				@Override
				public void awake(IJobChangeEvent event) {
				}

				@Override
				public void aboutToRun(IJobChangeEvent event) {
				}
			});
			job.schedule();
		} else {

		}
	}

	private synchronized boolean addToBatch(IDocument document) {
		if (batch != null) {
			batch.documents.add(document);
			return true;
		}
		return false;
	}

	private synchronized boolean addToBatch(IResource resource) {
		if (batch != null) {
			batch.resources.add(resource);
			return true;
		}
		return false;
	}

	public static BatchUpdater batch() {
		return INSTANCE.getBatch(null);
	}

	private synchronized BatchUpdater getBatch(BatchVerificationJob job) {
		if (batch == null) {
			return batch = new BatchUpdater(job) {

				@Override
				public void close() {
					performBatchUpdate();
				}

			};
		}
		batch.references++;
		return batch;
	}

}
