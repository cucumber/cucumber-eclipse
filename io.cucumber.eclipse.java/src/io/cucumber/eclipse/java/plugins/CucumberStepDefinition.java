package io.cucumber.eclipse.java.plugins;

import io.cucumber.plugin.event.StepDefinition;

/**
 * A cucumber StepDefinition with the coresponding code location
 * 
 * @author christoph
 *
 */
public final class CucumberStepDefinition {

	private StepDefinition stepDefinition;
	private CucumberCodeLocation codeLocation;

	public CucumberStepDefinition(StepDefinition stepDefinition, CucumberCodeLocation codeLocation) {
		this.stepDefinition = stepDefinition;
		this.codeLocation = codeLocation;
	}

	public CucumberCodeLocation getCodeLocation() {
		return codeLocation;
	}

	public StepDefinition getStepDefinition() {
		return stepDefinition;
	}

}
