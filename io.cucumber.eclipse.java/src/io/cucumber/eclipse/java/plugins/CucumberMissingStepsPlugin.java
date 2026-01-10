package io.cucumber.eclipse.java.plugins;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;

/**
 * This plugin collects code snippet suggestions for missing steps
 * 
 * @author christoph
 *
 */
public class CucumberMissingStepsPlugin implements Plugin, ConcurrentEventListener, EventListener {

	private Map<Integer, Collection<String>> snippets = new ConcurrentHashMap<>();

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetsSuggestedEvent);

	}

	private void handleSnippetsSuggestedEvent(SnippetsSuggestedEvent event) {
		Location stepLocation = event.getStepLocation();
		snippets.computeIfAbsent(stepLocation.getLine(), l -> ConcurrentHashMap.newKeySet())
				.addAll(event.getSuggestion().getSnippets());
	}

	public Map<Integer, Collection<String>> getSnippets() {
		return snippets;
	}

}
