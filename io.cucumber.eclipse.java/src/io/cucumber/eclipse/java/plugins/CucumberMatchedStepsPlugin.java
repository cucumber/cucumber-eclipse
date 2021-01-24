package io.cucumber.eclipse.java.plugins;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;

/**
 * A plugin that records matched test steps
 * 
 * @author christoph
 *
 */
public class CucumberMatchedStepsPlugin implements Plugin, ConcurrentEventListener, EventListener {

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
	}

	private void handleTestStepFinished(TestStepFinished event) {
		TestCase testCase = event.getTestCase();
		TestStep testStep = event.getTestStep();
		String codeLocation = testStep.getCodeLocation();
		if (codeLocation != null) {
			System.out.println("Matched:" + testCase.getKeyword() + ": " + testCase.getName() + " :: "
					+ testCase.getLocation().getLine()
					+ " --> " + codeLocation);
		}
	}

}
