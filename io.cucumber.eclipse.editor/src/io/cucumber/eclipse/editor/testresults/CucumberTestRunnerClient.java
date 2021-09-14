package io.cucumber.eclipse.editor.testresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.TestCase;
import io.cucumber.messages.Messages.TestCase.TestStep;
import io.cucumber.messages.Messages.TestCaseStarted;
import io.cucumber.messages.Messages.TestStepFinished;
import io.cucumber.messages.Messages.TestStepFinished.TestStepResult;
import io.cucumber.messages.Messages.TestStepFinished.TestStepResult.Status;
import io.cucumber.messages.Messages.TestStepStarted;

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
		System.out.println("startMonitoring()");
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
		if (env.hasTestRunStarted()) {
			session.notifyTestSessionStarted(null);
			return;
		}

		if (env.hasGherkinDocument()) {
			GherkinDocument document = env.getGherkinDocument();
			Feature feature = document.getFeature();
			// TODO get number of tests?
			session.newTestSuite(document.getUri(), feature.getName(), null, null, feature.getName(), "");
			return;
		}
		if (env.hasPickle()) {
			Pickle pickle = env.getPickle();
			pickles.put(pickle.getId(), pickle);
			return;
		}
		if (env.hasTestCase()) {
			// TODO support "parameterized" test for scenario outlines see Eclipse Bug
			// 573263
			TestCase testCase = env.getTestCase();
			testcases.put(testCase.getId(), testCase);
			Pickle pickle = pickles.get(testCase.getPickleId());
			// TODO match the pickle to the scenario/... item... and use this as the "method
			// name"
			Map<String, PickleStep> pickleSteps = pickle.getStepsList().stream()
					.collect(Collectors.toMap(PickleStep::getId, Function.identity()));
			ITestSuiteElement testSuite = session.newTestSuite(testCase.getId(), pickle.getAstNodeIdsList().toString(),
					testCase.getTestStepsCount(), (ITestSuiteElement) session.getTestElement(pickle.getUri()),
					pickle.getName(), env.toString());
			for (TestStep step : testCase.getTestStepsList()) {
				String pickleStepId = step.getPickleStepId();
				if (pickleStepId.isBlank()) {
					continue;
				}
				PickleStep pickleStep = pickleSteps.get(pickleStepId);
				session.newTestCase(step.getId(), pickleStep.getId(), testSuite, pickleStep.getText(), step.toString());
			}
			return;
		}
		if (env.hasTestCaseStarted()) {
			TestCaseStarted testCaseStarted = env.getTestCaseStarted();
			started.put(testCaseStarted.getId(), testCaseStarted);
			return;
		}
		if (env.hasTestStepStarted()) {
			TestStepStarted testStepStarted = env.getTestStepStarted();
			// TODO pass time-stamps see see Eclipse Bug 573264
			session.notifyTestStarted(session.getTestElement(testStepStarted.getTestStepId()));
			return;
		}
		if (env.hasTestStepFinished()) {
			// TODO pass time-stamps see see Eclipse Bug 573264
			TestStepFinished testStepFinished = env.getTestStepFinished();
			TestStepResult testStepResult = testStepFinished.getTestStepResult();
			Status status = testStepResult.getStatus();
			if (status == Status.PASSED || status == Status.PENDING || status == Status.SKIPPED) {
				session.notifyTestEnded(session.getTestElement(testStepFinished.getTestStepId()),
						status != Status.PASSED);
			} else {
				String expected = null;
				String actual = null;
				String message = testStepResult.getMessage();
				message = message.replace("\\r", "\r").replace("\\n", "\n").replace("\\t", "\t");
				Matcher matcher = OPENTEST4J.matcher(message);
				if (matcher.find()) {
					expected = matcher.group(1).strip();
					actual = matcher.group(2).strip();
				}
				FailureTrace trace = new FailureTrace(message, expected, actual);
				session.notifyTestFailed(session.getTestElement(testStepFinished.getTestStepId()),
						status == Status.FAILED ? Result.FAILURE : Result.ERROR, status == Status.UNDEFINED, trace);
			}
			return;
		}

		if (env.hasTestCaseFinished()) {
//			TestCaseFinished testCaseFinished = env.getTestCaseFinished();
//			TestCaseStarted testCaseStarted = started.get(testCaseFinished.getTestCaseStartedId());
		}
		if (env.hasTestRunFinished()) {
			// TODO pass duration?
			session.notifyTestSessionCompleted(null);
			return;
		}
	}
}
