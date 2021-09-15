package io.cucumber.eclipse.editor.document;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.cucumber.eclipse.editor.launching.EnvelopeListener;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.StepDefinition;
import io.cucumber.messages.Messages.TestCase;
import io.cucumber.messages.Messages.TestCase.TestStep;
import io.cucumber.messages.Messages.TestStepFinished;
import io.cucumber.messages.Messages.TestStepStarted;

/**
 * Handler that links the individual message parts together
 * 
 * @author christoph
 *
 */
public abstract class GherkinMessageHandler implements EnvelopeListener {

	private GherkinStream stream;

	private Map<String, PickleStepLink> pickleStepMap = new ConcurrentHashMap<>();
	private Map<String, TestStepLink> testStepMap = new ConcurrentHashMap<>();
	private Map<String, StepDefinition> stepDefinitionMap = new ConcurrentHashMap<>();

	@Override
	public void handleEnvelope(Envelope env) {
		if (env.hasTestCase()) {
			TestCase testCase = env.getTestCase();
			for (TestStep step : testCase.getTestStepsList()) {
				testStepMap.put(step.getId(), new TestStepLink(testCase, step));
			}
			return;
		}
		if (env.hasPickle()) {
			Pickle pickle = env.getPickle();
			for (PickleStep step : pickle.getStepsList()) {
				pickleStepMap.put(step.getId(), new PickleStepLink(pickle, step));
			}
			return;
		}
		if (env.hasGherkinDocument()) {
			stream = new GherkinStream(env);
			return;
		}
		if (env.hasStepDefinition()) {
			StepDefinition stepDefinition = env.getStepDefinition();
			stepDefinitionMap.put(stepDefinition.getId(), stepDefinition);
			return;
		}
		boolean testStepStarted = env.hasTestStepStarted();
		boolean testStepFinished = env.hasTestStepFinished();
		if (testStepStarted || testStepFinished) {
			TestStepLink stepLink;
			if (testStepStarted) {
				TestStepStarted stepStarted = env.getTestStepStarted();
				stepLink = testStepMap.get(stepStarted.getTestStepId());
			} else if (testStepFinished) {
				TestStepFinished stepFinished = env.getTestStepFinished();
				stepLink = testStepMap.get(stepFinished.getTestStepId());
			} else {
				stepLink = null;
			}
			if (stepLink != null) {
				stepLink.link().flatMap(link -> {
					return link.backtrace().map(backtrace -> {

						// TODO
						StepDefinition stepDefinition = stepLink.stepDefinitions().findFirst().orElse(null);
						return new TestStepEvent(backtrace.feature, backtrace.scenario, backtrace.step);
					});
				}).ifPresent(event -> {
					if (testStepStarted) {
						handleTestStepStart(event);
					} else {
						// TODO
					}
				});
			}
			return;
		}
	}

	protected abstract void handleTestStepStart(TestStepEvent event);

	private final class TestStepLink {

		private TestCase testCase;
		private TestStep testStep;

		public TestStepLink(TestCase testCase, TestStep step) {
			this.testCase = testCase;
			this.testStep = step;
		}

		Optional<PickleStepLink> link() {
			return Optional.ofNullable(pickleStepMap.get(testStep.getPickleStepId()));
		}

		Stream<StepDefinition> stepDefinitions() {
			return testStep.getStepDefinitionIdsList().stream().map(stepDefinitionMap::get).filter(Objects::nonNull);
		}
	}

	private final class PickleStepLink {

		private Pickle pickle;
		private PickleStep step;

		public PickleStepLink(Pickle pickle, PickleStep step) {
			this.pickle = pickle;
			this.step = step;
		}

		Optional<Backtrace> backtrace() {

			if (stream != null) {
				return step.getAstNodeIdsList().stream().flatMap(astId -> {
					return findByAst(astId);
				}).findAny();
			}
			return Optional.empty();
		}

		private Stream<Backtrace> findByAst(String astId) {
			return stream.getFeature().flatMap(feature -> {

				return GherkinStream.scenarios(feature).flatMap(scenario -> {

					return GherkinStream.scenarioSteps(scenario).filter(step -> step.getId().equals(astId)).findAny()
							.map(step -> {
								return new Backtrace(feature, scenario, step);

							}).stream();
				}).findAny();
			}).stream();
		}

	}

	private final class Backtrace {

		final Feature feature;
		final Scenario scenario;
		final Step step;

		public Backtrace(Feature feature, Scenario scenario, Step step) {
			this.feature = feature;
			this.scenario = scenario;
			this.step = step;
		}

	}

}
