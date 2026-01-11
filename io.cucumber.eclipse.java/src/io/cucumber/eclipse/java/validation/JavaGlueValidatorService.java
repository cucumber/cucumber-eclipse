package io.cucumber.eclipse.java.validation;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.validation.IGlueValidator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;

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
		// Delegate to JavaGlueValidator to create and run the validation job
		JavaGlueValidator.validate(editorDocument, monitor);
	}

	public Collection<CucumberStepDefinition> getAvailableSteps(IDocument document, IProgressMonitor monitor)
			throws InterruptedException {
		return JavaGlueValidator.getAvailableSteps(document, monitor);
	}

}
