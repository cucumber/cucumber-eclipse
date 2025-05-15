package io.cucumber.eclipse.editor.document;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.cucumber.eclipse.editor.launching.EnvelopeListener;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.TestCase;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepStarted;

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
		if (env.getTestCase().isPresent()) {
			TestCase testCase = env.getTestCase().get();
			for (TestStep step : testCase.getTestSteps()) {
				testStepMap.put(step.getId(), new TestStepLink(step));
			}
			return;
		}
		if (env.getPickle().isPresent()) {
			Pickle pickle = env.getPickle().get();
			for (PickleStep step : pickle.getSteps()) {
				pickleStepMap.put(step.getId(), new PickleStepLink(step));
			}
			return;
		}
		if (env.getGherkinDocument().isPresent()) {
			stream = new GherkinStream(env);
			return;
		}
		if (env.getStepDefinition().isPresent()) {
			StepDefinition stepDefinition = env.getStepDefinition().get();
			stepDefinitionMap.put(stepDefinition.getId(), stepDefinition);
			return;
		}
		boolean testStepStarted = env.getTestStepStarted().isPresent();
		boolean testStepFinished = env.getTestStepFinished().isPresent();
		if (testStepStarted || testStepFinished) {
			TestStepLink stepLink;
			if (testStepStarted) {
				TestStepStarted stepStarted = env.getTestStepStarted().get();
				stepLink = testStepMap.get(stepStarted.getTestStepId());
			} else if (testStepFinished) {
				TestStepFinished stepFinished = env.getTestStepFinished().get();
				stepLink = testStepMap.get(stepFinished.getTestStepId());
			} else {
				stepLink = null;
			}
			if (stepLink != null) {

				stepLink.link().flatMap(link -> {
					return link.backtrace().map(backtrace -> {

						// TODO
						StepDefinition stepDefinition = stepLink.stepDefinitions().findFirst().orElse(null);
						return new TestStepEvent(backtrace.feature, backtrace.scenario, backtrace.step,
								stepLink.testStep, stepDefinition);
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

		private TestStep testStep;

		public TestStepLink(TestStep step) {
			this.testStep = step;
		}

		Optional<PickleStepLink> link() {
			return testStep.getPickleStepId().flatMap(id-> Optional.ofNullable(pickleStepMap.get(id)));
		}

		Stream<StepDefinition> stepDefinitions() {
			return testStep.getStepDefinitionIds().map(l->l.stream().map(stepDefinitionMap::get).filter(Objects::nonNull)).orElse(Stream.of());
		}
	}

	private final class PickleStepLink {

		private PickleStep step;

		public PickleStepLink(PickleStep step) {
			this.step = step;
		}

		Optional<Backtrace> backtrace() {

			if (stream != null) {
				return step.getAstNodeIds().stream().flatMap(astId -> {
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
