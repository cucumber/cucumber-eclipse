package io.cucumber.eclipse.python.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.validation.DocumentValidator;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.eclipse.python.preferences.BehavePreferences;

/**
 * Provides access to Behave glue validation results and manages validation
 * jobs.
 * <p>
 * This class maintains a cache of validation jobs and their results (matched
 * steps) to support features like:
 * <ul>
 * <li>Navigation to step definitions (Ctrl+Click)</li>
 * <li>Code mining for step references</li>
 * </ul>
 * </p>
 * <p>
 * Validation is triggered by the
 * {@link io.cucumber.eclipse.editor.validation.DocumentValidator} through the
 * {@link BehaveGlueValidatorService}. This class no longer manages document
 * lifecycle or triggers validation directly.
 * </p>
 * 
 * @see BehaveGlueJob for the background validation implementation
 * @see BehaveGlueValidatorService for integration with the validation framework
 */
public class BehaveGlueValidator {

	/**
	 * Maps documents to their currently running or scheduled validation jobs.
	 * Thread-safe to allow concurrent access from UI and background threads.
	 */
	private static ConcurrentMap<IDocument, BehaveGlueJob> jobMap = new ConcurrentHashMap<>();

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
	 * Creates and runs a validation job for the given editor document.
	 * <p>
	 * This method is called by {@link BehaveGlueValidatorService} when the
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
		BehaveGlueJob job = jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null) {
				oldJob.cancel();
			}
			return new BehaveGlueJob(() -> editorDocument);
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
	 * This method is package-protected to allow BehaveGlueJob to register listeners
	 * when it has access to the resource.
	 * </p>
	 * 
	 * @param resource the resource to determine which preference scopes to listen
	 *                 to
	 */
	static synchronized void setupGlobalPreferenceListener(IResource resource) {
// Register workspace-level preference listener
		IPreferenceStore workspaceStore = Activator.getDefault().getPreferenceStore();
		if (!propertyChangeListeners.containsKey(workspaceStore)) {
			IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					DocumentValidator.revalidateAllDocuments();
				}
			};
			workspaceStore.addPropertyChangeListener(propertyListener);
			propertyChangeListeners.put(workspaceStore, propertyListener);
		}

// Register project-level preference listener if this resource has project-specific settings
		if (resource != null) {
			BehavePreferences prefs = BehavePreferences.of(resource);
			IEclipsePreferences projectNode = prefs.node();
			if (projectNode != null && !preferenceChangeListeners.containsKey(projectNode)) {
				IPreferenceChangeListener preferenceListener = new IPreferenceChangeListener() {
					@Override
					public void preferenceChange(PreferenceChangeEvent event) {
						DocumentValidator.revalidateAllDocuments();
					}
				};
				projectNode.addPreferenceChangeListener(preferenceListener);
				preferenceChangeListeners.put(projectNode, preferenceListener);
			}
		}
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

// Cancel and clean up all jobs
		jobMap.values().forEach(job -> {
			job.cancel();
		});
		jobMap.clear();
	}

	/**
	 * Retrieves the matched steps for the specified document.
	 * <p>
	 * This method provides access to step matching results from the most recent
	 * validation. Each {@link StepMatch} contains information about a Gherkin step
	 * and its corresponding step definition, enabling features like navigation
	 * (Ctrl+Click) to step definitions.
	 * </p>
	 * 
	 * @param document the document to get matched steps for
	 * @return a collection of matched steps, or an empty collection if:
	 *         <ul>
	 *         <li>The document is {@code null}</li>
	 *         <li>No validation has been performed yet</li>
	 *         <li>Validation failed or was cancelled</li>
	 *         </ul>
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
