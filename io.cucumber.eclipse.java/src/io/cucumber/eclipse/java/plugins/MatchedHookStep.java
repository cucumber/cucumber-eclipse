package io.cucumber.eclipse.java.plugins;

import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Location;

/**
 * A Hook matched to a step
 * 
 * @author christoph
 *
 */
public final class MatchedHookStep extends MatchedStep<HookTestStep> {
	public MatchedHookStep(HookTestStep hookTestStep, Location location) {
		super(hookTestStep, location, new CucumberCodeLocation(hookTestStep.getCodeLocation()));
	}
}
