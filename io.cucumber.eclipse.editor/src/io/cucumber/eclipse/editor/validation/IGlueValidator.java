package io.cucumber.eclipse.editor.validation;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;

/**
 * Interface for language-specific glue code validators.
 * <p>
 * Implementations of this interface provide backend-specific validation of
 * Gherkin feature files by matching steps with their corresponding step
 * definitions. Each validator is responsible for a specific language or
 * framework (e.g., Java/Cucumber, Python/Behave).
 * </p>
 * <p>
 * Validators are discovered via OSGi Declarative Services and managed by the
 * {@link DocumentValidator}, which handles document lifecycle and
 * coordinates validation across multiple backends.
 * </p>
 * 
 * @see DocumentValidator
 */
public interface IGlueValidator {

	/**
	 * Determines if this validator can validate the given resource.
	 * <p>
	 * This method is called by the {@link DocumentValidator} to identify which
	 * validators should be used for a particular feature file. A validator should
	 * return {@code true} if it can process the resource based on project
	 * configuration, language, or framework detection.
	 * </p>
	 * 
	 * @param resource the resource to check
	 * @return {@code true} if this validator can validate the resource,
	 *         {@code false} otherwise
	 * @throws CoreException if an error occurs while checking
	 */
	boolean canValidate(IResource resource) throws CoreException;

	/**
	 * Validates a collection of Gherkin editor documents.
	 * <p>
	 * This method performs the actual validation by:
	 * <ul>
	 * <li>Running the framework in validation/dry-run mode</li>
	 * <li>Matching steps with their definitions</li>
	 * <li>Creating markers for unmatched steps</li>
	 * <li>Caching results for navigation support</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Implementations should handle errors gracefully and should NOT throw
	 * exceptions for validation failures. Instead, create error markers to inform
	 * the user.
	 * </p>
	 * <p>
	 * Accepting a collection allows validators to optimize validation when multiple
	 * documents need to be validated, as runtime setup costs can be amortized
	 * across documents. Implementations may choose to validate documents
	 * sequentially or in batch depending on the backend capabilities.
	 * </p>
	 * 
	 * @param editorDocuments the documents to validate
	 * @param monitor         the progress monitor for cancellation and reporting
	 * @throws CoreException if a critical error occurs that prevents validation
	 */
	void validate(Collection<GherkinEditorDocument> editorDocuments, IProgressMonitor monitor) throws CoreException;

}
