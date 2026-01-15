package io.cucumber.eclipse.editor.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Background job that performs comprehensive validation of Gherkin documents.
 * <p>
 * This job performs validation in two stages:
 * <ol>
 * <li><b>Syntax Validation</b>: Checks the Gherkin syntax and creates markers
 * for parse errors. If syntax errors are found, glue validation is
 * skipped for that document.</li>
 * <li><b>Glue Validation</b>: For documents with valid syntax, delegates to all applicable
 * {@link IGlueValidator} implementations to validate step definitions. Documents
 * are grouped by their applicable validators to ensure each validator only
 * processes documents it can handle.</li>
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
		return family == VerificationJob.class || family == IGlueValidator.class;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Collection<GherkinEditorDocument> editorDocuments = getEditorDocuments();
		monitor.subTask("Validate " + editorDocuments.size() + " Documents");
		if (editorDocuments.isEmpty()) {
			return Status.OK_STATUS;
		}

		// Stage 1: Syntax validation
		List<GherkinEditorDocument> validDocuments = validateSyntax(editorDocuments, monitor);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		// Stage 2: Glue validation
		validateGlue(validDocuments, monitor);

		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	/**
	 * Validates the Gherkin syntax of all documents and creates markers for parse errors.
	 * 
	 * @param editorDocuments the documents to validate
	 * @param monitor the progress monitor for cancellation
	 * @return list of documents with valid syntax that should proceed to glue validation
	 */
	private List<GherkinEditorDocument> validateSyntax(Collection<GherkinEditorDocument> editorDocuments,
			IProgressMonitor monitor) {
		List<GherkinEditorDocument> validDocuments = new ArrayList<>();
		
		for (GherkinEditorDocument editorDocument : editorDocuments) {
			if (monitor.isCanceled()) {
				break;
			}

			IResource resource = editorDocument.getResource();
			if (resource == null) {
				continue;
			}

			List<ParseError> syntaxErrors = editorDocument.getParseError().collect(Collectors.toList());
			MarkerFactory.syntaxErrorOnGherkin(resource, syntaxErrors, false);

			// Only pass documents with valid syntax to glue validation
			if (syntaxErrors.isEmpty()) {
				validDocuments.add(editorDocument);
			}
		}
		
		return validDocuments;
	}

	/**
	 * Validates glue code by grouping documents by their applicable validators
	 * and calling each validator with only the documents it can handle.
	 * 
	 * @param validDocuments documents with valid syntax to validate
	 * @param monitor the progress monitor for cancellation
	 */
	private void validateGlue(List<GherkinEditorDocument> validDocuments, IProgressMonitor monitor) {
		if (validDocuments.isEmpty()) {
			return;
		}

		// Group documents by their applicable validators
		Map<IGlueValidator, List<GherkinEditorDocument>> validatorToDocuments = new LinkedHashMap<>();
		
		for (GherkinEditorDocument document : validDocuments) {
			if (monitor.isCanceled()) {
				break;
			}
			
			IResource resource = document.getResource();
			if (resource == null) {
				continue;
			}
			
			try {
				for (IGlueValidator validator : CucumberServiceRegistry.getGlueValidators(resource)) {
					validatorToDocuments.computeIfAbsent(validator, k -> new ArrayList<>()).add(document);
				}
			} catch (Exception e) {
				EditorLogging.error("Error determining validators for resource: " + resource.getFullPath(), e);
			}
		}

		// Validate each group of documents with their respective validator
		for (Map.Entry<IGlueValidator, List<GherkinEditorDocument>> entry : validatorToDocuments.entrySet()) {
			if (monitor.isCanceled()) {
				break;
			}
			
			IGlueValidator validator = entry.getKey();
			List<GherkinEditorDocument> documents = entry.getValue();
			
			try {
				validator.validate(documents, monitor);
			} catch (Exception e) {
				EditorLogging.error("Error during glue validation with " + validator.getClass().getName(), e);
			}
		}
	}

	/**
	 * Retrieves the GherkinEditorDocuments to validate.
	 * Subclasses implement this to provide the documents from their specific source
	 * (text buffer or resource).
	 * 
	 * @return the documents to validate, or an empty collection if not available
	 */
	protected abstract Collection<GherkinEditorDocument> getEditorDocuments();

}
