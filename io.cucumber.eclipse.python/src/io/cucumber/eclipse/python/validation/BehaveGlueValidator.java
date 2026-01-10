package io.cucumber.eclipse.python.validation;

import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.eclipse.python.preferences.BehavePreferences;

/**
 * Performs validation on Python/Behave feature files by running behave --dry-run
 * to verify step definition matching
 * 
 * @author copilot
 */
public class BehaveGlueValidator implements IDocumentSetupParticipant {

	private static ConcurrentMap<IDocument, BehaveGlueJob> jobMap = new ConcurrentHashMap<>();

	/**
	 * Global property change listeners for preference stores.
	 * Used to track and clean up workspace-level preference listeners.
	 */
	private static final Map<IPreferenceStore, IPropertyChangeListener> propertyChangeListeners = new HashMap<>();

	/**
	 * Global preference change listeners for Eclipse preference nodes.
	 * Used to track and clean up project-level preference listeners.
	 */
	private static final Map<IEclipsePreferences, IPreferenceChangeListener> preferenceChangeListeners = new HashMap<>();

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
	 * @param resource the resource to determine which preference scopes to listen to
	 */
	static synchronized void setupGlobalPreferenceListener(IResource resource) {
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
			BehavePreferences prefs = BehavePreferences.of(resource);
			IEclipsePreferences projectNode = prefs.node();
			if (projectNode != null && !preferenceChangeListeners.containsKey(projectNode)) {
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
	}

	/**
	 * Triggers revalidation for all currently known documents.
	 * <p>
	 * This method makes a copy of the current job map to avoid concurrent
	 * modification issues, then triggers validation for each document.
	 * </p>
	 */
	private static void revalidateAllDocuments() {
		// Make a copy to avoid concurrent modification
		Map<IDocument, BehaveGlueJob> snapshot = new HashMap<>(jobMap);
		snapshot.keySet().forEach(document -> validate(document, 0));
	}

	/**
	 * Cleans up global preference listeners and resources.
	 * <p>
	 * This method should be called when the plugin is stopping to ensure
	 * proper cleanup of all registered preference listeners.
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
