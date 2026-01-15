package io.cucumber.eclipse.java.validation;

import java.util.Collection;

import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;

/**
 * Container for glue validation results.
 * 
 * @param availableSteps all step definitions found in the project
 * @param matchedSteps steps that were successfully matched to step definitions
 */
public record GlueSteps(Collection<CucumberStepDefinition> availableSteps,
		Collection<MatchedStep<?>> matchedSteps) {
}