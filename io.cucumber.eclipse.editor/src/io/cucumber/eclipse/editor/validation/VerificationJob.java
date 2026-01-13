package io.cucumber.eclipse.editor.validation;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import io.cucumber.eclipse.editor.CucumberServiceRegistry;
import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.messages.types.ParseError;

/**
 * Background job that performs comprehensive validation of a Gherkin document.
 * <p>
 * This job performs validation in two stages:
 * <ol>
 * <li><b>Syntax Validation</b>: Checks the Gherkin syntax and creates markers
 * for parse errors. If syntax errors are found, glue validation is
 * skipped.</li>
 * <li><b>Glue Validation</b>: If syntax is valid, delegates to all applicable
 * {@link IGlueValidator} implementations to validate step definitions.</li>
 * </ol>
 * </p>
 * <p>
 * This job is reused for multiple validation runs on the same document. When
 * validation is requested while a previous run is in progress, the job is
 * rescheduled to run again after the current run completes.
 * </p>
 */
abstract class VerificationJob extends Job {

	VerificationJob(String name) {
		super("Verify " + name);
	}

	/**
	 * Returns true if this job belongs to the specified family.
	 * All VerificationJob instances belong to the VerificationJob.class family,
	 * allowing bulk operations on verification jobs.
	 * 
	 * @param family the job family identifier
	 * @return true if this job belongs to the family
	 */
	@Override
	public boolean belongsTo(Object family) {
		return family == VerificationJob.class;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		GherkinEditorDocument editorDocument = getEditorDocument();
		if (editorDocument == null) {
			return Status.OK_STATUS;
		}

		IResource resource = editorDocument.getResource();
		if (resource == null) {
			return Status.OK_STATUS;
		}

		// Stage 1: Syntax validation
		List<ParseError> syntaxErrors = editorDocument.getParseError().collect(Collectors.toList());
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		MarkerFactory.syntaxErrorOnGherkin(resource, syntaxErrors, false);

		// Stage 2: Glue validation (only if no syntax errors)
		if (syntaxErrors.isEmpty()) {
			try {
				for (IGlueValidator validator : CucumberServiceRegistry.getGlueValidators(resource)) {
					if (monitor.isCanceled()) {
						break;
					}
					validator.validate(editorDocument, monitor);
				}
			} catch (Exception e) {
				EditorLogging.error("Error during glue validation", e);
			}
		}

		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	/**
	 * Retrieves the GherkinEditorDocument to validate.
	 * Subclasses implement this to provide the document from their specific source
	 * (text buffer or resource).
	 * 
	 * @return the document to validate, or null if not available
	 */
	protected abstract GherkinEditorDocument getEditorDocument();

}
