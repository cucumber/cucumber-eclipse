package io.cucumber.eclipse.java.plugins;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;

/**
 * A plugin that records matched test steps
 * 
 * @author christoph
 *
 */
public class CucumberMatchedStepsPlugin implements Plugin, ConcurrentEventListener, EventListener {

	private Map<URI, Collection<MatchedStep<?>>> matchedStepsByFeature = new ConcurrentHashMap<>();

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
	}

	private void handleTestStepFinished(TestStepFinished event) {
		URI featureUri = event.getTestCase().getUri();
		TestStep testStep = event.getTestStep();
		if (testStep instanceof PickleStepTestStep) {
			PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
			if (pickleStepTestStep.getCodeLocation() != null) {
				matchedStepsByFeature.computeIfAbsent(featureUri, k -> ConcurrentHashMap.newKeySet())
						.add(new MatchedPickleStep(pickleStepTestStep));
			}
		} else if (testStep instanceof HookTestStep) {
			HookTestStep hookTestStep = (HookTestStep) testStep;
			matchedStepsByFeature.computeIfAbsent(featureUri, k -> ConcurrentHashMap.newKeySet())
					.add(new MatchedHookStep(hookTestStep, event.getTestCase().getLocation()));
		}
	}

	public Collection<MatchedStep<?>> getMatchedStepsForFeature(URI featureUri) {
		return matchedStepsByFeature.getOrDefault(featureUri, List.of());
	}

}
