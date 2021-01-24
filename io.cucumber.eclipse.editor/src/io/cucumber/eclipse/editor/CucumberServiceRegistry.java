package io.cucumber.eclipse.editor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.cucumber.eclipse.editor.steps.IStepDefinitionGenerator;

/**
 * The {@link CucumberServiceRegistry} gives access to the extensions provided
 * by language providers
 * 
 * @author christoph
 *
 */
@Component(service = {})
public class CucumberServiceRegistry {

	private static List<IStepDefinitionGenerator> stepDefinitionGenerators = new CopyOnWriteArrayList<>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addStepDefinitionGenerator(IStepDefinitionGenerator generator) {
		stepDefinitionGenerators.add(generator);
	}

	void removeStepDefinitionGenerator(IStepDefinitionGenerator generator) {
		stepDefinitionGenerators.remove(generator);
	}

	public static List<IStepDefinitionGenerator> getStepDefinitionGenerators() {
		return Collections.unmodifiableList(stepDefinitionGenerators);
	}
}
