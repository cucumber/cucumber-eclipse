package io.cucumber.eclipse.java.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.properties.CucumberJavaBackendProperties;

/**
 * Provides access to Java glue validation results and manages validation jobs.
 * <p>
 * This class maintains a cache of validation jobs and their results (matched
 * steps, available step definitions) to support features like:
 * <ul>
 * <li>Navigation to step definitions (Ctrl+Click)</li>
 * <li>Code mining for step references</li>
 * <li>Content assist</li>
 * </ul>
 * </p>
 * <p>
 * Validation is triggered by the
 * {@link io.cucumber.eclipse.editor.validation.DocumentValidator} through the
 * {@link JavaGlueValidatorService}. This class no longer manages document
 * lifecycle or triggers validation directly.
 * </p>
 * 
 * @see JavaGlueJob for the background validation implementation
 * @see JavaGlueValidatorService for integration with the validation framework
 */
public class JavaGlueValidator {

	/**
	 * Maps documents to their currently running or scheduled validation jobs.
	 * Thread-safe to allow concurrent access from UI and background threads.
	 */
	private static ConcurrentMap<IDocument, JavaGlueJob> jobMap = new ConcurrentHashMap<>();

	/**
	 * Global property change listeners for preference stores. Used to track and
	 * clean up workspace-level preference listeners.
	 */
	private static final Map<IPreferenceStore, IPropertyChangeListener> propertyChangeListeners = new HashMap<>();

	/**
	 * Global preference change listeners for Eclipse preference nodes. Used to
	 * track and clean up project-level preference listeners.
	 */
	private static final Map<IEclipsePreferences, IPreferenceChangeListener> preferenceChangeListeners = new HashMap<>();

	/**
	 * Global element changed listener for tracking Java model changes. Initialized
	 * once and cleaned up on shutdown.
	 */
	private static GlueCodeChangeListener glueCodeChangeListener;

	/**
	 * Creates and runs a validation job for the given editor document.
	 * <p>
	 * This method is called by {@link JavaGlueValidatorService} when the
	 * DocumentValidator triggers validation. It manages the job lifecycle, ensuring
	 * only one job runs at a time per document.
	 * </p>
	 * 
	 * @param editorDocument the document to validate
	 * @param monitor        the progress monitor
	 * @throws CoreException if validation fails
	 */
	static void validate(GherkinEditorDocument editorDocument, IProgressMonitor monitor) throws CoreException {
		IDocument document = editorDocument.getDocument();
		JavaGlueJob job = jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null) {
				oldJob.cancel();
			}
			return new JavaGlueJob(oldJob, () -> editorDocument);
		});

// Run the job directly in this thread (called from DocumentValidator's background job)
		job.run(monitor);
	}

	/**
	 * Sets up a global preference listener that triggers revalidation for all
	 * currently known documents when preferences change.
	 * <p>
	 * This method is idempotent - it only registers listeners once for each
	 * preference scope. The listeners are registered at both the workspace and
	 * project-specific preference levels.
	 * </p>
	 * <p>
	 * This method is package-protected to allow JavaGlueJob to register listeners
	 * when it has access to the resource.
	 * </p>
	 * 
	 * @param resource the resource to determine which preference scopes to listen
	 *                 to
	 */
	static synchronized void setupGlobalPreferenceListener(org.eclipse.core.resources.IResource resource) {
// Register workspace-level preference listener
		IPreferenceStore workspaceStore = Activator.getDefault().getPreferenceStore();
		if (!propertyChangeListeners.containsKey(workspaceStore)) {
			IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					revalidateAllDocuments();
				}
			};
			workspaceStore.addPropertyChangeListener(propertyListener);
			propertyChangeListeners.put(workspaceStore, propertyListener);
		}

// Register project-level preference listener if this resource has project-specific settings
		if (resource != null) {
			CucumberJavaBackendProperties properties = CucumberJavaBackendProperties.of(resource);
			IEclipsePreferences projectNode = properties.node();
			if (!preferenceChangeListeners.containsKey(projectNode)) {
				IPreferenceChangeListener preferenceListener = new IPreferenceChangeListener() {
					@Override
					public void preferenceChange(PreferenceChangeEvent event) {
						revalidateAllDocuments();
					}
				};
				projectNode.addPreferenceChangeListener(preferenceListener);
				preferenceChangeListeners.put(projectNode, preferenceListener);
			}
		}
// Register global element changed listener if not already registered
		if (glueCodeChangeListener == null) {
			glueCodeChangeListener = new GlueCodeChangeListener();
			JavaCore.addElementChangedListener(glueCodeChangeListener);
		}
	}

	/**
	 * Triggers revalidation for all currently known documents.
	 * <p>
	 * This method delegates to
	 * {@link io.cucumber.eclipse.editor.validation.DocumentValidator#revalidateAllDocuments()}
	 * which manages all feature file documents.
	 * </p>
	 */
	private static void revalidateAllDocuments() {
		io.cucumber.eclipse.editor.validation.DocumentValidator.revalidateAllDocuments();
	}

	/**
	 * Cleans up global preference listeners and resources.
	 * <p>
	 * This method should be called when the plugin is stopping to ensure proper
	 * cleanup of all registered preference listeners.
	 * </p>
	 */
	public static synchronized void shutdown() {
// Remove all property change listeners
		propertyChangeListeners.forEach((store, listener) -> {
			store.removePropertyChangeListener(listener);
		});
		propertyChangeListeners.clear();

// Remove all preference change listeners
		preferenceChangeListeners.forEach((node, listener) -> {
			node.removePreferenceChangeListener(listener);
		});
		preferenceChangeListeners.clear();

// Remove element changed listener
		if (glueCodeChangeListener != null) {
			JavaCore.removeElementChangedListener(glueCodeChangeListener);
			glueCodeChangeListener = null;
		}

// Cancel and clean up all jobs
		jobMap.values().forEach(job -> {
			job.cancel();
		});
		jobMap.clear();
	}

	/**
	 * Synchronizes with the current validation job for the specified document.
	 * <p>
	 * This method blocks the calling thread until the validation job completes or a
	 * timeout occurs (30 seconds). It is used internally by methods that need to
	 * access validation results synchronously, such as
	 * {@link #getMatchedSteps(IDocument, IProgressMonitor)}.
	 * </p>
	 * 
	 * @param document the document to synchronize on
	 * @param monitor  the progress monitor for cancellation support, or
	 *                 {@code null} if cancellation is not needed. No progress is
	 *                 reported to this monitor.
	 * @return the validation job that was synchronized with, or {@code null} if no
	 *         job exists
	 * @throws OperationCanceledException if the operation was cancelled via the
	 *                                    monitor
	 * @throws InterruptedException       if the thread was interrupted while
	 *                                    waiting
	 */
	private static JavaGlueJob sync(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		JavaGlueJob glueJob = jobMap.get(document);
		if (glueJob != null) {
			glueJob.join(TimeUnit.SECONDS.toMillis(30), monitor);
		}
		return glueJob;
	}

	/**
	 * Retrieves the matched steps for the specified document.
	 * <p>
	 * This method provides access to step matching results from the most recent
	 * validation. It waits for any ongoing validation to complete before returning
	 * the results. Each {@link MatchedStep} contains information about a Gherkin
	 * step and its corresponding step definition, enabling features like navigation
	 * (Ctrl+Click) to step definitions.
	 * </p>
	 * 
	 * @param document the document to get matched steps for
	 * @param monitor  the progress monitor for cancellation support, or
	 *                 {@code null}
	 * @return a collection of matched steps, or an empty collection if:
	 *         <ul>
	 *         <li>The document is {@code null}</li>
	 *         <li>No validation has been performed yet</li>
	 *         <li>Validation failed or was cancelled</li>
	 *         </ul>
	 * @throws OperationCanceledException if the operation was cancelled via the
	 *                                    monitor
	 * @throws InterruptedException       if the thread was interrupted while
	 *                                    waiting for validation
	 */
	public static Collection<MatchedStep<?>> getMatchedSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		if (document != null) {
			JavaGlueJob job = sync(document, monitor);
			if (job != null) {
				return job.matchedSteps;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves all available step definitions discovered for the specified
	 * document.
	 * <p>
	 * This method provides access to all step definitions found during validation,
	 * regardless of whether they match any steps in the current document. It waits
	 * for any ongoing validation to complete before returning the results. The
	 * returned step definitions can be used for content assist, code completion, or
	 * analysis.
	 * </p>
	 * 
	 * @param document the document to get available step definitions for
	 * @param monitor  the progress monitor for cancellation support, or
	 *                 {@code null}
	 * @return a collection of all discovered step definitions, or an empty
	 *         collection if:
	 *         <ul>
	 *         <li>The document is {@code null}</li>
	 *         <li>No validation has been performed yet</li>
	 *         <li>Validation failed or was cancelled</li>
	 *         <li>No step definitions were found in the project</li>
	 *         </ul>
	 * @throws OperationCanceledException if the operation was cancelled via the
	 *                                    monitor
	 * @throws InterruptedException       if the thread was interrupted while
	 *                                    waiting for validation
	 */
	public static Collection<CucumberStepDefinition> getAvailableSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		if (document != null) {
			JavaGlueJob job = sync(document, monitor);
			if (job != null) {
				return job.parsedSteps;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Revalidates all feature file documents currently open in the specified
	 * project.
	 * <p>
	 * This method is called by {@link GlueCodeChangeListener} when Java glue code
	 * changes are detected. It triggers revalidation through the DocumentValidator.
	 * </p>
	 * <p>
	 * This method is package-protected to allow the listener to trigger
	 * revalidation while keeping the implementation details encapsulated.
	 * </p>
	 * 
	 * @param project the project whose feature files should be revalidated
	 * @throws CoreException if project access fails
	 */
	static void revalidateProject(IProject project) throws CoreException {
// Delegate to DocumentValidator which manages all documents
		io.cucumber.eclipse.editor.validation.DocumentValidator.revalidateAllDocuments();
	}

}
