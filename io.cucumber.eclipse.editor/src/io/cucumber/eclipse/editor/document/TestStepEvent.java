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
 * Event that captures the complete context of a test step execution.
 * <p>
 * This class links together the different layers of a Cucumber test:
 * <ul>
 * <li>The Gherkin feature, scenario, and step definitions</li>
 * <li>The test step from the test execution</li>
 * <li>The step definition implementation</li>
 * </ul>
 * </p>
 * <p>
 * Used primarily for debugging support to construct stack frames
 * that navigate between Gherkin source and step implementations.
 * </p>
 * 
 * @author christoph
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

	/**
	 * @return the feature containing the test step
	 */
	public Feature getFeature() {
		return feature;
	}

	/**
	 * @return the scenario containing the test step
	 */
	public Scenario getScenario() {
		return scenario;
	}

	/**
	 * @return the Gherkin step definition
	 */
	public Step getStep() {
		return step;
	}

	/**
	 * Constructs a debug stack trace for this test step.
	 * <p>
	 * The stack trace includes frames for:
	 * <ol>
	 * <li>The step implementation (with step definition)</li>
	 * <li>The scenario</li>
	 * <li>The feature</li>
	 * </ol>
	 * </p>
	 * 
	 * @param thread the debug thread to associate frames with
	 * @return an array of stack frames representing the execution context
	 */
	public IStackFrame[] getStackTrace(IThread thread) {
		GherkingStackFrame stepFrame = new GherkingStepStackFrame(thread, testStep, step, stepDefinition);

		GherkingStackFrame scenarioFrame = new GherkingStackFrame(thread, scenario.getLocation().getLine().intValue(),
				"[" + scenario.getKeyword().strip() + "] " + scenario.getName());
		GherkingStackFrame featureFrame = new GherkingStackFrame(thread, feature.getLocation().getLine().intValue(),
				"[" + feature.getKeyword().strip() + "] " + feature.getName());
		return new IStackFrame[] { stepFrame, scenarioFrame, featureFrame };
	}



}
