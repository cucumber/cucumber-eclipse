package io.cucumber.eclipse.python.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.eclipse.python.launching.BehaveProcessLauncher;
import io.cucumber.eclipse.python.preferences.BehavePreferences;

/**
 * Background job that runs behave --dry-run to validate step definitions
 */
final class BehaveGlueJob extends Job {

	// Pattern to match step definition lines like:
	// @given('I have a calculator')             # calculator_steps.py:19
	private static final Pattern STEP_DEF_PATTERN = Pattern
			.compile("@\\w+\\('(.+?)'\\)\\s+#\\s+(.+?):(\\d+)");

	// Pattern to match step usage lines like:
	//   Given I have a calculator               # ../calculator.feature:7
	private static final Pattern STEP_USAGE_PATTERN = Pattern
			.compile("\\s+(Given|When|Then|And|But)\\s+(.+?)\\s+#\\s+(.+?):(\\d+)");

	private Supplier<GherkinEditorDocument> documentSupplier;
	private volatile Collection<StepMatch> matchedSteps = Collections.emptyList();

	BehaveGlueJob(Supplier<GherkinEditorDocument> documentSupplier) {
		super("Verify Behave Glue Code");
		this.documentSupplier = documentSupplier;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		GherkinEditorDocument editorDocument = documentSupplier.get();
		if (editorDocument == null) {
			return Status.CANCEL_STATUS;
		}

		IResource resource = editorDocument.getResource();
		if (resource == null) {
			return Status.CANCEL_STATUS;
		}

		IProject project = resource.getProject();
		if (project == null) {
			return Status.CANCEL_STATUS;
		}

		try {
			// Get behave command from preferences
			BehavePreferences preferences = BehavePreferences.of(resource);
			String behaveCommand = preferences.behaveCommand();
			
			// Run behave --dry-run --format steps.usage --no-summary
			String workingDir = project.getLocation().toOSString();
			String featurePath = resource.getLocation().toOSString();

			BehaveProcessLauncher launcher = new BehaveProcessLauncher()
				.withCommand(behaveCommand)
				.withFeaturePath(featurePath)
				.withWorkingDirectory(workingDir)
				.withDryRun(true)
				.withFormat("steps.usage")
				.withNoSummary(true);

			Process process = launcher.launch();

			// Parse the output
			Map<Integer, StepMatch> stepMatchMap = new HashMap<>();
			Map<Integer, String> unmatchedSteps = new HashMap<>();
			String currentStepPattern = null;
			String currentStepFile = null;
			int currentStepLine = -1;

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (monitor.isCanceled()) {
						process.destroy();
						return Status.CANCEL_STATUS;
					}

					// Check if this is a step definition line
					Matcher defMatcher = STEP_DEF_PATTERN.matcher(line);
					if (defMatcher.find()) {
						currentStepPattern = defMatcher.group(1);
						currentStepFile = defMatcher.group(2);
						currentStepLine = Integer.parseInt(defMatcher.group(3));
						continue;
					}

					// Check if this is a step usage line
					Matcher usageMatcher = STEP_USAGE_PATTERN.matcher(line);
					if (usageMatcher.find() && currentStepPattern != null) {
						String stepText = usageMatcher.group(2);
						String featureFile = usageMatcher.group(3);
						int featureLine = Integer.parseInt(usageMatcher.group(4));

						// Only record matches for the current feature file
						if (featureFile.contains(resource.getName())) {
							StepMatch match = new StepMatch(featureLine, stepText, currentStepFile, currentStepLine,
									currentStepPattern);
							stepMatchMap.put(featureLine, match);
						}
					}
				}
			}

			// Wait for process to complete
			int exitCode = process.waitFor();

			// Find all steps in the feature and determine which ones are unmatched
			// For now, we'll just use the steps we found
			matchedSteps = new ArrayList<>(stepMatchMap.values());

			// Create markers for unmatched steps
			// Collect snippets for steps that don't have matches
			Map<Integer, Collection<String>> snippets = new HashMap<>();

			// For demonstration, we'll assume all steps in the feature that aren't in stepMatchMap are unmatched
			// In a real implementation, we'd parse the feature document to find all steps
			
			// Update markers
			MarkerFactory.missingSteps(resource, snippets, Activator.PLUGIN_ID, false);

			return Status.OK_STATUS;

		} catch (IOException | InterruptedException e) {
			ILog.get().error("Behave validation failed", e);
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Behave validation failed", e);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public Collection<StepMatch> getMatchedSteps() {
		return matchedSteps;
	}
}
