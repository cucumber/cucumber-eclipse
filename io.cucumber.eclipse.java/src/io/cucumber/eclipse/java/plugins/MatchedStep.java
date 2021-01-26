package io.cucumber.eclipse.java.plugins;

import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStep;

/**
 * Holds data about matching feature steps to source code locations
 * 
 * @author christoph
 *
 */
public final class MatchedStep {

	private final TestStep testStep;
	private final Location location;
	private final CucumberCodeLocation codeLocation;

	public MatchedStep(PickleStepTestStep step) {
		this.testStep = step;
		location = step.getStep().getLocation();
		codeLocation = new CucumberCodeLocation(testStep.getCodeLocation());
	}

	public MatchedStep(HookTestStep hookTestStep, Location location) {
		testStep = hookTestStep;
		this.location = location;
		codeLocation = new CucumberCodeLocation(testStep.getCodeLocation());
	}

	public Location getLocation() {
		return location;
	}
	
	public TestStep getTestStep() {
		return testStep;
	}

	public CucumberCodeLocation getCodeLocation() {
		return codeLocation;
	}

}
