package io.cucumber.eclipse.python.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.validation.IGlueValidator;
import io.cucumber.eclipse.python.launching.BehaveProcessLauncher;

/**
 * Python/Behave backend implementation of {@link IGlueValidator}.
 * <p>
 * This validator integrates with the Behave testing framework to validate
 * Cucumber feature files against Python step definitions. It runs behave in
 * dry-run mode to match Gherkin steps with their corresponding Python
 * functions.
 * </p>
 * 
 * @see BehaveGlueValidator for the complete validation infrastructure
 * @see BehaveGlueJob for the actual validation implementation
 */
@Component(service = IGlueValidator.class)
public class BehaveGlueValidatorService implements IGlueValidator {

	@Override
	public boolean canValidate(IResource resource) throws CoreException {
		if (resource == null) {
			return false;
		}
		return BehaveProcessLauncher.isBehaveProject(resource);
	}

	@Override
	public void validate(GherkinEditorDocument editorDocument, IProgressMonitor monitor) throws CoreException {
		// Delegate to BehaveGlueValidator to create and run the validation job
		BehaveGlueValidator.validate(editorDocument, monitor);
	}

}
