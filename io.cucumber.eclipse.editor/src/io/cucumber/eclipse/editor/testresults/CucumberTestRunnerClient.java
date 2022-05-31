package io.cucumber.eclipse.editor.testresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.unittest.launcher.ITestRunnerClient;
import org.eclipse.unittest.model.ITestElement.FailureTrace;
import org.eclipse.unittest.model.ITestElement.Result;
import org.eclipse.unittest.model.ITestRunSession;
import org.eclipse.unittest.model.ITestSuiteElement;

import io.cucumber.eclipse.editor.launching.EnvelopeListener;
import io.cucumber.eclipse.editor.launching.EnvelopeProvider;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.TestCase;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.messages.types.TestStepStarted;

/**
 * {@link ITestRunnerClient} that converts cucumber events into appropriate
 * calls to the test session
 * 
 * @author christoph
 *
 */
public class CucumberTestRunnerClient implements ITestRunnerClient, EnvelopeListener {

	private static final Pattern OPENTEST4J = Pattern.compile("expected:(.*)but was:(.*)");

	private final ITestRunSession session;
	private Map<String, Pickle> pickles = new HashMap<>();
	private Map<String, TestCase> testcases = new HashMap<>();
	private Map<String, TestCaseStarted> started = new HashMap<>();
	private List<EnvelopeProvider> registered = new ArrayList<>();

	CucumberTestRunnerClient(ITestRunSession session) {
		this.session = session;
	}

	@Override
	public void stopTest() {
		// TODO check EnvelopeProvider if we can stop them?
	}

	@Override
	public void stopMonitoring() {
		CompletableFuture.supplyAsync(() -> {

			for (EnvelopeProvider ep : registered) {
				ep.removeEnvelopeListener(this);
			}
			return null;
		});
	}

	@Override
	public void startMonitoring() {
		ILaunch launch = session.getLaunch();
		IProcess[] processes = launch.getProcesses();
		for (IProcess process : processes) {
			if (process instanceof EnvelopeProvider) {
				EnvelopeProvider ep = (EnvelopeProvider) process;
				registered.add(ep);
				ep.addEnvelopeListener(this);

			}
		}

	}

	@Override
	public void handleEnvelope(Envelope env) {
		if (env.getTestRunStarted().isPresent()) {
			session.notifyTestSessionStarted(null);
			return;
		}

		if (env.getGherkinDocument().isPresent()) {
			GherkinDocument document = env.getGherkinDocument().get();
			Feature feature = document.getFeature().get();
			// TODO get number of tests?
			session.newTestSuite(document.getUri().get(), feature.getName(), null, null, feature.getName(), "");
			return;
		}
		if (env.getPickle().isPresent()) {
			Pickle pickle = env.getPickle().get();
			pickles.put(pickle.getId(), pickle);
			return;
		}
		if (env.getTestCase().isPresent()) {
			// TODO support "parameterized" test for scenario outlines see Eclipse Bug
			// 573263
			TestCase testCase = env.getTestCase().get();
			testcases.put(testCase.getId(), testCase);
			Pickle pickle = pickles.get(testCase.getPickleId());
			// TODO match the pickle to the scenario/... item... and use this as the "method
			// name"
			Map<String, PickleStep> pickleSteps = pickle.getSteps().stream()
					.collect(Collectors.toMap(PickleStep::getId, Function.identity()));
			ITestSuiteElement testSuite = session.newTestSuite(testCase.getId(), pickle.getAstNodeIds().toString(),
					testCase.getTestSteps().size(), (ITestSuiteElement) session.getTestElement(pickle.getUri()),
					pickle.getName(), env.toString());
			for (TestStep step : testCase.getTestSteps()) {
				Optional<String> pickleStepId = step.getPickleStepId();
				if (pickleStepId.isEmpty()|| pickleStepId.get().isBlank()) {
					continue;
				}
				PickleStep pickleStep = pickleSteps.get(pickleStepId.get());
				session.newTestCase(step.getId(), pickleStep.getId(), testSuite, pickleStep.getText(), step.toString());
			}
			return;
		}
		if (env.getTestCaseStarted().isPresent()) {
			TestCaseStarted testCaseStarted = env.getTestCaseStarted().get();
			started.put(testCaseStarted.getId(), testCaseStarted);
			return;
		}
		if (env.getTestStepStarted().isPresent()) {
			TestStepStarted testStepStarted = env.getTestStepStarted().get();
			// TODO pass time-stamps see see Eclipse Bug 573264
			session.notifyTestStarted(session.getTestElement(testStepStarted.getTestStepId()));
			return;
		}
		if (env.getTestStepFinished().isPresent()) {
			// TODO pass time-stamps see see Eclipse Bug 573264
			TestStepFinished testStepFinished = env.getTestStepFinished().get();
			TestStepResult testStepResult = testStepFinished.getTestStepResult();
			TestStepResultStatus status = testStepResult.getStatus();
			if (status == TestStepResultStatus.PASSED || status == TestStepResultStatus.PENDING || status == TestStepResultStatus.SKIPPED) {
				session.notifyTestEnded(session.getTestElement(testStepFinished.getTestStepId()),
						status != TestStepResultStatus.PASSED);
			} else {
				String expected = null;
				String actual = null;
				String message = testStepResult.getMessage().get();
				message = message.replace("\\r", "\r").replace("\\n", "\n").replace("\\t", "\t");
				Matcher matcher = OPENTEST4J.matcher(message);
				if (matcher.find()) {
					expected = matcher.group(1).strip();
					actual = matcher.group(2).strip();
				}
				FailureTrace trace = new FailureTrace(message, expected, actual);
				session.notifyTestFailed(session.getTestElement(testStepFinished.getTestStepId()),
						status == TestStepResultStatus.FAILED ? Result.FAILURE : Result.ERROR, status == TestStepResultStatus.UNDEFINED, trace);
			}
			return;
		}

		if (env.getTestCaseFinished().isPresent()) {
//			TestCaseFinished testCaseFinished = env.getTestCaseFinished();
//			TestCaseStarted testCaseStarted = started.get(testCaseFinished.getTestCaseStartedId());
		}
		if (env.getTestRunFinished().isPresent()) {
			// TODO pass duration?
			session.notifyTestSessionCompleted(null);
			return;
		}
	}
}
