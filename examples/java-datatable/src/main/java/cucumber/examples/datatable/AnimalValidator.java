package cucumber.examples.datatable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.core.gherkin.DataTableArgument;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;

/**
 * This validator is enabled in the feature by using
 * <code>#validation-plugin: cucumber.examples.datatable.AnimalValidator</code>
 */
public class AnimalValidator implements Plugin, ConcurrentEventListener {

	private ConcurrentHashMap<Integer, String> errors = new ConcurrentHashMap<>();

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
	}

	private void handleTestStepFinished(TestStepFinished event) {
		TestStep testStep = event.getTestStep();
		if (testStep instanceof PickleStepTestStep) {
			PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
			Step step = pickleStepTestStep.getStep();
			if ("the animal {string}".equals(pickleStepTestStep.getPattern())) {
				StepArgument argument = step.getArgument();
				Animals animal = loadAnimal(pickleStepTestStep.getDefinitionArgument().get(0).getValue());
				if (animal == null) {
					// Invalid animal!
					return;
				}
				if (argument instanceof DataTableArgument dataTable) {
					List<String> availableData = animal.getAvailableData();
					List<List<String>> cells = dataTable.cells();
					for (int i = 1; i < cells.size(); i++) {
						int line = dataTable.getLine() + i;
						List<String> list = cells.get(i);
						String vv = list.get(0);
						if (!animal.getAvailableDataForAnimals().contains(vv)) {
							errors.put(line, vv + " is not valid for any animal");
						} else if (!availableData.contains(vv)) {
							errors.put(line, vv + " is not valid for animal " + animal.getClass().getSimpleName());
						}
					}
				}
			}
		}
	}
	//This is a magic method called by cucumber-eclipse to fetch the final errors and display them in the document
	public Map<Integer, String> getValidationErrors() {
		return errors;
	}

	private Animals loadAnimal(String value) {
		try {
			Class<?> clz = getClass().getClassLoader()
					.loadClass("cucumber.examples.datatable." + value.replace("\"", ""));
			Object instance = clz.getConstructor().newInstance();
			if (instance instanceof Animals anml) {
				return anml;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
