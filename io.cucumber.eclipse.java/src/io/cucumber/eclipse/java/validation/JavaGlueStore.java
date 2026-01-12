package io.cucumber.eclipse.java.validation;

import java.util.Collection;

import org.eclipse.jface.text.IDocument;

import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.MatchedStep;

public interface JavaGlueStore {

	Collection<CucumberStepDefinition> getAvailableSteps(IDocument document);

	Collection<MatchedStep<?>> getMatchedSteps(IDocument document);

}
