package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.osgi.service.component.annotations.Component;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.eclipse.editor.console.CucumberConsole;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberEclipsePlugin;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.TestCaseStarted;
import io.cucumber.messages.Messages.TestStepFinished;
import io.cucumber.messages.Messages.TestStepStarted;
import io.cucumber.messages.Messages.Timestamp;
import io.cucumber.tagexpressions.Expression;
import mnita.ansiconsole.preferences.AnsiConsolePreferenceUtils;

/**
 * Launches documents using the {@link CucumberRuntime}
 * 
 * @author christoph
 *
 */
@Component(service = ILauncher.class)
public class CucumberRuntimeLauncher implements ILauncher {

	@Override
	public void launch(Map<GherkinEditorDocument, IStructuredSelection> launchMap, Mode mode, boolean temporary,
			IProgressMonitor monitor) throws OperationCanceledException, CoreException {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = lm.getLaunchConfigurationType(CucumberFeatureLaunchConstants.TYPE_ID);
		SubMonitor subMonitor = SubMonitor.convert(monitor, launchMap.size() * 100);
		for (Entry<GherkinEditorDocument, IStructuredSelection> entry : launchMap.entrySet()) {
			GherkinEditorDocument document = entry.getKey();
			IResource resource = document.getResource();
			IJavaProject javaProject = JDTUtil.getJavaProject(resource);
			if (javaProject == null) {
				continue;
			}
			ILaunchConfiguration lc = getLaunchConfiguration(javaProject, resource, type);
			String identifier = mode.getLaunchMode().getIdentifier();
			if (temporary) {
				ILaunchConfigurationWorkingCopy copy = lc.getWorkingCopy();
				List<FeatureWithLines> featuresWithLines = new ArrayList<>();
				List<Expression> filters = new ArrayList<>();
				IStructuredSelection selection = entry.getValue();
				for (Object object : selection) {
					if (object instanceof Scenario) {
						Scenario scenario = (Scenario) object;
						URI locationURI = resource.getLocationURI();
						featuresWithLines.add(FeatureWithLines.create(locationURI,
								Collections.singleton(scenario.getLocation().getLine())));
					} else if (object instanceof Expression) {
						filters.add((Expression) object);
					}
				}
				copy.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_WITH_LINE,
						featuresWithLines.stream().map(FeatureWithLines::toString).collect(Collectors.joining(" ")));
				copy.setAttribute(CucumberFeatureLaunchConstants.ATTR_TAGS,
						filters.stream()
								.map(Expression::toString).collect(Collectors.joining(" and ")));
				copy.launch(identifier, subMonitor.slice(100));
			} else {
				lc.launch(identifier, subMonitor.slice(100));
			}
		}
	}

	private ILaunchConfiguration getLaunchConfiguration(IJavaProject javaProject, IResource resource,
			ILaunchConfigurationType type) throws CoreException {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		String featurePath = resource.getProjectRelativePath().toPortableString();
		for (ILaunchConfiguration configuration : lm.getLaunchConfigurations(type)) {
			String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			if (projectName.equals(javaProject.getElementName())) {
				if (featurePath
						.equals(configuration.getAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, ""))) {
//					configuration.delete();
					 return configuration;
				}
			}
		}
		ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
				lm.generateLaunchConfigurationName(resource.getName()));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, javaProject.getElementName());
		wc.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePath);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Dcucumber.publish.quiet=true");
		TEST_RESULT_LISTENER_CONFIGURER.apply(wc);
		return wc.doSave();
	}

	@Override
	public boolean supports(IResource resource) {
		return JDTUtil.isJavaProject(resource.getProject());
	}

	@Override
	public boolean supports(Mode mode) {
		return mode == Mode.DEBUG || mode == Mode.RUN || mode == Mode.PROFILE;
	}

	public static void runFeaturesEmbedded(IJavaProject javaProject, List<Feature> features,
			Collection<FeatureWithLines> featureFilter, Mode mode, CucumberConsole console, IProgressMonitor monitor,
			Collection<Expression> tagFilters) throws CoreException {
		try (CucumberRuntime cucumberRuntime = CucumberRuntime.create(javaProject)) {
			CucumberEclipsePlugin plugin = new CucumberEclipsePlugin(new Consumer<Envelope>() {

				private Map<String, TestStepPerfInfo> map = new HashMap<>();
				private GherkinDocument gherkinDocument;

				@Override
				public void accept(Envelope envelope) {
//					System.out.println(envelope);
					if (envelope.hasTestCaseStarted()) {
						TestCaseStarted testCaseStarted = envelope.getTestCaseStarted();
						String testCaseId = testCaseStarted.getTestCaseId();
//						System.out.println("Testcase started: " + testCaseId);
					}
					// TODO publish it
					if (envelope.hasTestStepFinished()) {
						TestStepFinished stepFinished = envelope.getTestStepFinished();
						map.get(stepFinished.getTestStepId()).end = stepFinished.getTimestamp();
					} else if (envelope.hasTestStepStarted()) {
						TestStepStarted stepStarted = envelope.getTestStepStarted();
						// stepStarted.getTestCaseStartedId()
						map.put(stepStarted.getTestStepId(), new TestStepPerfInfo(stepStarted.getTimestamp()));
					} else if (envelope.hasGherkinDocument()) {
						gherkinDocument = envelope.getGherkinDocument();
					} else if (envelope.hasTestRunFinished()) {
						// Feature id maps to the pickle -> ast_node_ids
//						gherkinDocument.getFeature().getChildrenList().stream().filter(FeatureChild::hasScenario)
//								.map(FeatureChild::getScenario).forEach(s -> System.out.println(s.getId()));

//						gherkinDocument.getFeature().getChildrenList().stream().filter(FeatureChild::hasScenario)
//								.map(FeatureChild::getScenario).flatMap(scenario -> scenario.getStepsList().stream())
//								.distinct();
					}

				}
			});
			for (Feature feature : features) {
				cucumberRuntime.addFeature(feature);
			}
			RuntimeOptionsBuilder options = cucumberRuntime.getRuntimeOptions();
			for (FeatureWithLines featureWithLines : featureFilter) {
				options.addFeature(featureWithLines);
			}
			for (Expression tagFilter : tagFilters) {
				options.addTagFilter(tagFilter);
			}
			options.setPublishQuiet(true);
			// TODO other options
			options.addDefaultSummaryPrinterIfAbsent();
			options.setThreads(java.lang.Runtime.getRuntime().availableProcessors());
			options.setMonochrome(!AnsiConsolePreferenceUtils.isAnsiConsoleEnabled());
			cucumberRuntime.addPlugin(plugin);
			try {
				try (IOConsoleOutputStream stream = console.newOutputStream()) {
					cucumberRuntime.run(monitor, new PrintStream(stream));
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CucumberRuntimeLauncher.class, "launch failed", e));
			}
		}
	}

	private static final class TestStepPerfInfo {

		private Timestamp start;
		protected Timestamp end;

		public TestStepPerfInfo(Timestamp timestamp) {
			this.start = timestamp;
		}

	}

}
