package io.cucumber.eclipse.editor.document;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import io.cucumber.eclipse.editor.debug.GherkingStackFrame;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;

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

	public TestStepEvent(Feature feature, Scenario scenario, Step step) {
		this.feature = feature;
		this.scenario = scenario;
		this.step = step;
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
		GherkingStackFrame stepFrame = new GherkingStackFrame(thread, step.getLocation().getLine(), step.getText());
		GherkingStackFrame scenarioFrame = new GherkingStackFrame(thread, scenario.getLocation().getLine(),
				scenario.getName());
		GherkingStackFrame featureFrame = new GherkingStackFrame(thread, feature.getLocation().getLine(),
				feature.getName());
		return new IStackFrame[] { stepFrame, scenarioFrame, featureFrame };
	}

}
