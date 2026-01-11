package io.cucumber.eclipse.editor.validation;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
 * for parse errors. If syntax errors are found, glue validation is skipped.</li>
 * <li><b>Glue Validation</b>: If syntax is valid, delegates to all applicable
 * {@link IGlueValidator} implementations to validate step definitions.</li>
 * </ol>
 * </p>
 * <p>
 * The job ensures that only one validation runs at a time for a given document
 * by waiting for any previous job to complete before starting.
 * </p>
 */
class VerificationJob extends Job {

	private VerificationJob oldJob;
	private GherkinEditorDocument editorDocument;

	VerificationJob(VerificationJob oldJob, GherkinEditorDocument editorDocument) {
		super("Verify Gherkin Document");
		this.oldJob = oldJob;
		this.editorDocument = editorDocument;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Wait for any previous job to complete
		if (oldJob != null) {
			try {
				oldJob.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Status.CANCEL_STATUS;
			}
		}

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
						return Status.CANCEL_STATUS;
					}
					try {
						validator.validate(editorDocument, monitor);
					} catch (CoreException e) {
						EditorLogging.error("Glue validation failed for validator: " + validator.getClass().getName(),
								e);
					}
				}
			} catch (Exception e) {
				EditorLogging.error("Error during glue validation", e);
			}
		}

		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

}
