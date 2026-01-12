package io.cucumber.eclipse.java.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.validation.DocumentValidator;
import io.cucumber.eclipse.editor.validation.IGlueValidator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;
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
@Component(service = { IGlueValidator.class, JavaGlueValidatorService.class })
public class JavaGlueValidatorService implements IGlueValidator {

	private final Map<IEclipsePreferences, IPreferenceChangeListener> preferenceChangeListeners = new HashMap<>();

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
	public void validate(GherkinEditorDocument editorDocument, IProgressMonitor monitor) throws CoreException {
		IResource resource = editorDocument.getResource();
		listenForChanges(resource);
		// Delegate to JavaGlueValidator to create and run the validation job
		JavaGlueValidator.validate(editorDocument, monitor);
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

	public Collection<CucumberStepDefinition> getAvailableSteps(IDocument document, IProgressMonitor monitor)
			throws InterruptedException {
		return JavaGlueValidator.getAvailableSteps(document, monitor);
	}

	public Collection<MatchedStep<?>> getMatchedSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		return JavaGlueValidator.getMatchedSteps(document, monitor);
	}

	@Deactivate
	public synchronized void shutdown() {
		preferenceChangeListeners.forEach(IEclipsePreferences::removePreferenceChangeListener);
		if (glueCodeChangeListener != null) {
			JavaCore.removeElementChangedListener(glueCodeChangeListener);
		}
	}

}
