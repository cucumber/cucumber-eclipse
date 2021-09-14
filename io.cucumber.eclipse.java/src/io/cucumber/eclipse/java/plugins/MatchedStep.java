package io.cucumber.eclipse.java.plugins;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestStep;

/**
 * Holds data about matching feature steps to source code locations
 * 
 * @author christoph
 *
 */
public abstract class MatchedStep<T extends TestStep> {

	private final T testStep;

	private final Location location;
	private final CucumberCodeLocation codeLocation;

	MatchedStep(T testStep, Location location, CucumberCodeLocation codeLocation) {
		this.testStep = testStep;
		this.location = location;
		this.codeLocation = codeLocation;
	}

	public final Location getLocation() {
		return location;
	}

	public final T getTestStep() {
		return testStep;
	}

	public final CucumberCodeLocation getCodeLocation() {
		return codeLocation;
	}

	@Override
	public String toString() {
		return testStep + ": " + location + " -> " + codeLocation;
	}

}
