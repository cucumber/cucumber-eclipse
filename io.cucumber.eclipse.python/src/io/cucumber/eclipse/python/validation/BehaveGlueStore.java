package io.cucumber.eclipse.python.validation;

import java.util.Collection;

import org.eclipse.jface.text.IDocument;

/**
 * Interface for accessing Behave glue validation results.
 * <p>
 * Provides access to step matching information from the most recent validation.
 * </p>
 */
public interface BehaveGlueStore {

	/**
	 * Retrieves the matched steps for the specified document.
	 * 
	 * @param document the document to get matched steps for
	 * @return a collection of matched steps, or an empty collection if no validation has been performed yet
	 */
	Collection<StepMatch> getMatchedSteps(IDocument document);

}
