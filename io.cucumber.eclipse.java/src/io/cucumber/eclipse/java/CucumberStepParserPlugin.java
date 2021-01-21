package io.cucumber.eclipse.java;

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
		String location = definition.getLocation();
		int braceIndex = location.indexOf('(');
		if (braceIndex > 0) {
			location = location.substring(0, braceIndex);
		}
		int indexOf = location.lastIndexOf('.');
		String key = indexOf > 0 ? location.substring(0, indexOf) : "";
		stepList.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(definition);
	}

	/**
	 * @return a Map of locations to recorded step definitions
	 */
	public Map<String, Collection<StepDefinition>> getStepList() {
		return stepList;
	}



}
