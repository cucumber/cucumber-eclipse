package io.cucumber.eclipse.java.plugins;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
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

	private Map<URI, Map<Integer, Collection<String>>> snippetsByFeature = new ConcurrentHashMap<>();

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetsSuggestedEvent);

	}

	private void handleSnippetsSuggestedEvent(SnippetsSuggestedEvent event) {
		URI featureUri = event.getUri();
		Location stepLocation = event.getStepLocation();
		snippetsByFeature.computeIfAbsent(featureUri, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(stepLocation.getLine(), l -> ConcurrentHashMap.newKeySet())
				.addAll(event.getSnippets());
	}

	public Map<Integer, Collection<String>> getSnippetsForFeature(URI featureUri) {
		return snippetsByFeature.getOrDefault(featureUri, Map.of());
	}

}
