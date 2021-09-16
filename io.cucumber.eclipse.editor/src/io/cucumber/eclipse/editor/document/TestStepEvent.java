package io.cucumber.eclipse.editor.document;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import io.cucumber.eclipse.editor.debug.GherkingGroupValue;
import io.cucumber.eclipse.editor.debug.GherkingStackFrame;
import io.cucumber.eclipse.editor.debug.GherkingStepDefinitionValue;
import io.cucumber.eclipse.editor.debug.GherkingStepVariable;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.StepDefinition;
import io.cucumber.messages.Messages.TestCase.TestStep;

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
		GherkingStackFrame stepFrame = new GherkingStackFrame(thread, step.getLocation().getLine(),
				"[" + step.getKeyword().strip() + "] " + step.getText());
		if (stepDefinition != null) {
			System.out.println(stepDefinition);
			GherkingStepDefinitionValue value = new GherkingStepDefinitionValue(stepFrame, stepDefinition,
					step.getText());
			stepFrame.addVariable(new GherkingStepVariable(stepFrame, stepDefinition.getPattern().getSource(), value));
			addGroups(stepFrame, value::addVariable);
		} else {
			addGroups(stepFrame, stepFrame::addVariable);
		}
		GherkingStackFrame scenarioFrame = new GherkingStackFrame(thread, scenario.getLocation().getLine(),
				"[" + scenario.getKeyword().strip() + "] " + scenario.getName());
		GherkingStackFrame featureFrame = new GherkingStackFrame(thread, feature.getLocation().getLine(),
				"[" + feature.getKeyword().strip() + "] " + feature.getName());
		return new IStackFrame[] { stepFrame, scenarioFrame, featureFrame };
	}

	private void addGroups(GherkingStackFrame stepFrame, Consumer<IVariable> variableConsumer) {
		AtomicInteger counter = new AtomicInteger();
		testStep.getStepMatchArgumentsListsList().stream().flatMap(list -> list.getStepMatchArgumentsList().stream())
				.forEach(argument -> {
					String type = argument.getParameterTypeName();
					variableConsumer.accept(new GherkingStepVariable(stepFrame, "arg" + counter.get(),
							new GherkingGroupValue(stepFrame.getDebugTarget(), type, argument.getGroup())));
				});
	}

}
