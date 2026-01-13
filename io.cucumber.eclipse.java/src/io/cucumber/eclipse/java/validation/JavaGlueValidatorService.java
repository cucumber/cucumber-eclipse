package io.cucumber.eclipse.java.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferences;
import io.cucumber.eclipse.java.properties.CucumberJavaBackendProperties;

/**
 * Java/JVM backend implementation of {@link IGlueValidator}.
 * <p>
 * This validator integrates with the Eclipse Java Development Tools (JDT) to
 * validate Cucumber feature files against Java step definitions. It runs
 * Cucumber in dry-run mode to match Gherkin steps with their corresponding Java
 * methods.
 * </p>
 * 
 * @see JavaGlueValidator for the complete validation infrastructure
 * @see JavaGlueJob for the actual validation implementation
 */
@Component(service = { IGlueValidator.class, JavaGlueStore.class })
public class JavaGlueValidatorService implements IGlueValidator, JavaGlueStore, IGherkinDocumentListener {

	private final Map<IEclipsePreferences, IPreferenceChangeListener> preferenceChangeListeners = new HashMap<>();

	private final Map<IDocument, GlueSteps> glueMap = new ConcurrentHashMap<>();

	private GlueCodeChangeListener glueCodeChangeListener;

	@Override
	public boolean canValidate(IResource resource) throws CoreException {
		if (resource == null) {
			return false;
		}
		IJavaProject javaProject = JDTUtil.getJavaProject(resource);
		return javaProject != null && javaProject.exists();
	}

	@Override
	public void validate(Collection<GherkinEditorDocument> editorDocuments, IProgressMonitor monitor)
			throws CoreException {
		// Group documents by Java project for efficient batch processing
		Map<IJavaProject, List<GherkinEditorDocument>> documentsByProject = new HashMap<>();
		
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
			
			// Group by Java project
			IJavaProject javaProject = JDTUtil.getJavaProject(resource);
			if (javaProject != null && javaProject.exists()) {
				documentsByProject.computeIfAbsent(javaProject, k -> new java.util.ArrayList<>()).add(editorDocument);
			}
		}
		
		// Process each Java project's documents with shared runtime setup
		for (Map.Entry<IJavaProject, List<GherkinEditorDocument>> entry : documentsByProject.entrySet()) {
			if (monitor.isCanceled()) {
				break;
			}
			
			IJavaProject javaProject = entry.getKey();
			List<GherkinEditorDocument> documents = entry.getValue();
			
			// Get project preferences once for all documents in this project
			CucumberJavaPreferences projectPreferences = CucumberJavaPreferences.of(javaProject.getProject());
			
			// Validate each document with the shared project context
			for (GherkinEditorDocument document : documents) {
				if (monitor.isCanceled()) {
					break;
				}
				
				GlueSteps glueSteps = JavaGlueJob.validateGlue(document, javaProject, projectPreferences, monitor);
				if (glueSteps != null) {
					glueMap.put(document.getDocument(), glueSteps);
					EditorReconciler.reconcileFeatureEditor(document.getDocument());
				}
			}
		}
	}

	private synchronized void listenForChanges(IResource resource) {
		CucumberJavaBackendProperties properties = CucumberJavaBackendProperties.of(resource);
		IEclipsePreferences projectNode = properties.node();
		if (projectNode != null && !preferenceChangeListeners.containsKey(projectNode)) {
			IPreferenceChangeListener preferenceListener = event -> DocumentValidator
					.revalidateDocuments(resource.getProject());
			projectNode.addPreferenceChangeListener(preferenceListener);
			preferenceChangeListeners.put(projectNode, preferenceListener);
		}
		if (glueCodeChangeListener == null) {
			glueCodeChangeListener = new GlueCodeChangeListener();
			JavaCore.addElementChangedListener(glueCodeChangeListener);
		}
	}

	@Override
	public Collection<CucumberStepDefinition> getAvailableSteps(IDocument document) {
		GlueSteps glueSteps = glueMap.get(document);
		if (glueSteps == null) {
			return List.of();
		}
		return glueSteps.availableSteps();
	}

	@Override
	public Collection<MatchedStep<?>> getMatchedSteps(IDocument document) {
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
		if (glueCodeChangeListener != null) {
			JavaCore.removeElementChangedListener(glueCodeChangeListener);
		}
	}

	static record GlueSteps(Collection<CucumberStepDefinition> availableSteps,
			Collection<MatchedStep<?>> matchedSteps) {
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
