package io.cucumber.eclipse.java.plugins;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.StepDefinition;

/**
 * Cucumber plugin that collects all steps registered during a cucumber run
 * 
 * @author christoph
 *
 */
public class CucumberStepParserPlugin implements Plugin, ConcurrentEventListener, EventListener {

	private Map<String, Collection<StepDefinition>> stepList = new ConcurrentHashMap<>();

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(StepDefinedEvent.class, this::handleStepDefinedEvent);
	}

	private void handleStepDefinedEvent(StepDefinedEvent event) {
		StepDefinition definition = event.getStepDefinition();
		CucumberCodeLocation location = new CucumberCodeLocation(definition.getLocation());
		stepList.computeIfAbsent(location.getTypeName(), k -> ConcurrentHashMap.newKeySet()).add(definition);
	}

	/**
	 * @return a Map of locations to recorded step definitions
	 */
	public Map<String, Collection<StepDefinition>> getStepList() {
		return stepList;
	}



}
