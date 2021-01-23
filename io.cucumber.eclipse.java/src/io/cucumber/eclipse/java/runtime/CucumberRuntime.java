package io.cucumber.eclipse.java.runtime;

import java.util.Optional;
import java.util.UUID;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.messages.GherkinMessagesFeatureParser;
import io.cucumber.core.resource.Resource;

/**
 * Gives access to the cucumber-jvm runtime with special handling for
 * classloading
 * 
 * @author christoph
 *
 */
public class CucumberRuntime {

	private static final FeatureParser FEATURE_PARSER = new FeatureParser(UUID::randomUUID);

	public static Optional<Feature> loadFeature(Resource resource) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(GherkinMessagesFeatureParser.class.getClassLoader());
			return FEATURE_PARSER.parseResource(resource);
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}
	}
}
