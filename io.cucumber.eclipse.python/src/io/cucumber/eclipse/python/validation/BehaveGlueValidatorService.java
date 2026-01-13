package io.cucumber.eclipse.python.validation;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
	public void validate(Collection<GherkinEditorDocument> editorDocuments, IProgressMonitor monitor)
			throws CoreException {
		// For now, validate documents one by one
		// Future optimization: batch validation with single behave process
		for (GherkinEditorDocument editorDocument : editorDocuments) {
			if (monitor.isCanceled()) {
				break;
			}
			// Delegate to BehaveGlueValidator to create and run the validation job
			BehaveGlueValidator.validate(editorDocument, monitor);
		}
	}

}
