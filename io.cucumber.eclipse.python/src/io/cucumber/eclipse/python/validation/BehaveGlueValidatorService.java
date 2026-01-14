package io.cucumber.eclipse.python.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jface.text.IDocument;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import io.cucumber.eclipse.editor.EditorReconciler;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.editor.document.IGherkinDocumentListener;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.editor.validation.DocumentValidator;
import io.cucumber.eclipse.editor.validation.IGlueValidator;
import io.cucumber.eclipse.python.launching.BehaveProcessLauncher;
import io.cucumber.eclipse.python.preferences.BehavePreferences;

/**
 * Python/Behave backend implementation of {@link IGlueValidator}.
 * <p>
 * This validator integrates with the Behave testing framework to validate
 * Cucumber feature files against Python step definitions. It runs behave in
 * dry-run mode to match Gherkin steps with their corresponding Python
 * functions.
 * </p>
 * 
 * @see BehaveGlueJob for the actual validation implementation
 */
@Component(service = { IGlueValidator.class, BehaveGlueStore.class })
public class BehaveGlueValidatorService implements IGlueValidator, BehaveGlueStore, IGherkinDocumentListener {

	private final Map<IEclipsePreferences, IPreferenceChangeListener> preferenceChangeListeners = new HashMap<>();
	
	private final Map<IDocument, GlueSteps> glueMap = new ConcurrentHashMap<>();

	@Override
	public boolean canValidate(IResource resource) throws CoreException {
		if (resource == null) {
			return false;
		}
		return BehaveProcessLauncher.isBehaveProject(resource);
	}

	@Override
	public void validate(Collection<GherkinEditorDocument> editorDocuments, IProgressMonitor monitor)
			throws CoreException {
		// Group documents by project for efficient batch processing
		Map<IProject, List<GherkinEditorDocument>> documentsByProject = new HashMap<>();
		
		for (GherkinEditorDocument editorDocument : editorDocuments) {
			if (monitor.isCanceled()) {
				break;
			}
			
			IResource resource = editorDocument.getResource();
			if (resource == null) {
				continue;
			}
			
			// Clear glue validation errors for this resource
			MarkerFactory.clearGlueValidationError(resource, "glue_validation_error");
			
			// Set up change listeners
			listenForChanges(resource);
			
			// Group by project
			IProject project = resource.getProject();
			if (project != null && project.exists()) {
				documentsByProject.computeIfAbsent(project, k -> new ArrayList<>()).add(editorDocument);
			}
		}
		
		// Process each project's documents with shared behave setup
		for (Map.Entry<IProject, List<GherkinEditorDocument>> entry : documentsByProject.entrySet()) {
			if (monitor.isCanceled()) {
				break;
			}
			
			IProject project = entry.getKey();
			List<GherkinEditorDocument> documents = entry.getValue();
			
			// Get project preferences once for all documents in this project
			BehavePreferences projectPreferences = BehavePreferences.of(project);
			
			// Validate all documents in this project together
			Map<GherkinEditorDocument, GlueSteps> results = BehaveGlueJob.validateGlue(
					documents, project, projectPreferences, monitor);
			
			// Store results in glue map
			for (Map.Entry<GherkinEditorDocument, GlueSteps> result : results.entrySet()) {
				GherkinEditorDocument document = result.getKey();
				GlueSteps glueSteps = result.getValue();
				if (glueSteps != null) {
					glueMap.put(document.getDocument(), glueSteps);
					EditorReconciler.reconcileFeatureEditor(document.getDocument());
				}
			}
		}
	}

	private synchronized void listenForChanges(IResource resource) {
		BehavePreferences preferences = BehavePreferences.of(resource);
		IEclipsePreferences projectNode = preferences.node();
		if (projectNode != null && !preferenceChangeListeners.containsKey(projectNode)) {
			IPreferenceChangeListener preferenceListener = event -> DocumentValidator
					.revalidateDocuments(resource.getProject());
			projectNode.addPreferenceChangeListener(preferenceListener);
			preferenceChangeListeners.put(projectNode, preferenceListener);
		}
	}

	@Override
	public Collection<StepMatch> getMatchedSteps(IDocument document) {
		GlueSteps glueSteps = glueMap.get(document);
		if (glueSteps == null) {
			return List.of();
		}
		return glueSteps.matchedSteps();
	}

	@Activate
	void register() {
		GherkinEditorDocumentManager.addDocumentListener(this);
	}

	@Deactivate
	synchronized void shutdown() {
		GherkinEditorDocumentManager.removeDocumentListener(this);
		preferenceChangeListeners.forEach(IEclipsePreferences::removePreferenceChangeListener);
	}

	static record GlueSteps(Collection<StepMatch> matchedSteps) {
	}

	@Override
	public void documentCreated(IDocument document) {
	}

	@Override
	public void documentChanged(IDocument document) {
	}

	@Override
	public void documentDisposed(IDocument document) {
		glueMap.remove(document);
	}

}
