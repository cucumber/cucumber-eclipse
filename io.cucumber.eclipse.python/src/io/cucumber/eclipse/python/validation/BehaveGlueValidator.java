package io.cucumber.eclipse.python.validation;

import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;

/**
 * Provides access to Behave glue validation results.
 * <p>
 * This class serves as a bridge between the OSGi service (BehaveGlueStore)
 * and consumers like {@link IStepDefinitionOpener} that need access to
 * step matching results.
 * </p>
 * 
 * @see BehaveGlueValidatorService for the main validation logic
 * @see BehaveGlueJob for the background validation implementation
 */
@Component
public class BehaveGlueValidator {

	private static volatile BehaveGlueStore store;

	@Reference
	void setStore(BehaveGlueStore store) {
		BehaveGlueValidator.store = store;
	}

	void unsetStore(BehaveGlueStore store) {
		BehaveGlueValidator.store = null;
	}

	/**
	 * Retrieves the matched steps for the specified document.
	 * <p>
	 * This method provides access to step matching results from the most recent
	 * validation. Each {@link StepMatch} contains information about a Gherkin step
	 * and its corresponding step definition, enabling features like navigation
	 * (Ctrl+Click) to step definitions.
	 * </p>
	 * 
	 * @param document the document to get matched steps for
	 * @return a collection of matched steps, or an empty collection if:
	 *         <ul>
	 *         <li>The document is {@code null}</li>
	 *         <li>No validation has been performed yet</li>
	 *         <li>Validation failed or was cancelled</li>
	 *         </ul>
	 */
	public static Collection<StepMatch> getMatchedSteps(IDocument document) {
		BehaveGlueStore currentStore = store;
		if (currentStore != null && document != null) {
			return currentStore.getMatchedSteps(document);
		}
		return java.util.Collections.emptyList();
	}
}
