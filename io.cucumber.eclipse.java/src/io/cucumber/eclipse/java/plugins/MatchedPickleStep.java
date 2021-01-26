package io.cucumber.eclipse.java.plugins;

import io.cucumber.plugin.event.PickleStepTestStep;

/**
 * A matched step
 * 
 * @author christoph
 *
 */
public final class MatchedPickleStep extends MatchedStep<PickleStepTestStep> {

	public MatchedPickleStep(PickleStepTestStep pickleStep) {
		super(pickleStep, pickleStep.getStep().getLocation(), new CucumberCodeLocation(pickleStep.getCodeLocation()));
	}
}
