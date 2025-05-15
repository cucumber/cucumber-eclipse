package io.cucumber.eclipse.editor.document;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import io.cucumber.eclipse.editor.debug.GherkingStackFrame;
import io.cucumber.eclipse.editor.debug.GherkingStepStackFrame;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.TestStep;

/**
 * Event collecting the whole context of a given test step
 * 
 * @author christoph
 *
 */
public class TestStepEvent {

	private Feature feature;
	private Scenario scenario;
	private Step step;
	private TestStep testStep;
	private StepDefinition stepDefinition;

	TestStepEvent(Feature feature, Scenario scenario, Step step, TestStep testStep, StepDefinition stepDefinition) {
		this.feature = feature;
		this.scenario = scenario;
		this.step = step;
		this.testStep = testStep;
		this.stepDefinition = stepDefinition;
	}

	public Feature getFeature() {
		return feature;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public Step getStep() {
		return step;
	}

	public IStackFrame[] getStackTrace(IThread thread) {
		GherkingStackFrame stepFrame = new GherkingStepStackFrame(thread, testStep, step, stepDefinition);

		GherkingStackFrame scenarioFrame = new GherkingStackFrame(thread, scenario.getLocation().getLine().intValue(),
				"[" + scenario.getKeyword().strip() + "] " + scenario.getName());
		GherkingStackFrame featureFrame = new GherkingStackFrame(thread, feature.getLocation().getLine().intValue(),
				"[" + feature.getKeyword().strip() + "] " + feature.getName());
		return new IStackFrame[] { stepFrame, scenarioFrame, featureFrame };
	}



}
