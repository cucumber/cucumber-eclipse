package io.cucumber.eclipse.python.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	
	// Pattern to match undefined step lines like:
	//   When I add 2 and 3 numbers              # features/calculator.feature:8
	private static final Pattern UNDEFINED_STEP_PATTERN = Pattern
			.compile("\\s+(Given|When|Then|And|But)\\s+(.+?)\\s+#\\s+(.+?):(\\d+)");
	
	// Pattern to match snippet decorator lines like:
	// @when(u'I add 2 and 3 numbers')
	private static final Pattern SNIPPET_DECORATOR_PATTERN = Pattern
			.compile("@(given|when|then)\\(u?'(.+?)'\\)");

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
			Map<String, Integer> undefinedStepsMap = new HashMap<>(); // step text -> line number
			Map<String, String> snippetsMap = new HashMap<>(); // step text -> snippet
			
			String currentStepPattern = null;
			String currentStepFile = null;
			int currentStepLine = -1;
			
			boolean inUndefinedSection = false;
			boolean inSnippetSection = false;
			StringBuilder currentSnippet = null;
			String currentSnippetStepText = null;

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (monitor.isCanceled()) {
						process.destroy();
						return Status.CANCEL_STATUS;
					}

					// Check for section headers
					if (line.startsWith("UNDEFINED STEPS[")) {
						inUndefinedSection = true;
						inSnippetSection = false;
						continue;
					} else if (line.contains("You can implement step definitions for undefined steps with these snippets:")) {
						inUndefinedSection = false;
						inSnippetSection = true;
						continue;
					} else if (line.startsWith("UNUSED STEP DEFINITIONS[") || 
							   (line.trim().isEmpty() && inUndefinedSection)) {
						inUndefinedSection = false;
					}
					
					// Parse undefined steps section
					if (inUndefinedSection) {
						Matcher undefinedMatcher = UNDEFINED_STEP_PATTERN.matcher(line);
						if (undefinedMatcher.find()) {
							String stepText = undefinedMatcher.group(2).trim();
							String featureFile = undefinedMatcher.group(3);
							int featureLine = Integer.parseInt(undefinedMatcher.group(4));
							
							// Only record for the current feature file
							if (featureFile.contains(resource.getName())) {
								undefinedStepsMap.put(stepText, featureLine);
							}
						}
						continue;
					}
					
					// Parse snippet section
					if (inSnippetSection) {
						// Check if this is a snippet decorator line
						Matcher snippetMatcher = SNIPPET_DECORATOR_PATTERN.matcher(line);
						if (snippetMatcher.find()) {
							// Save previous snippet if exists
							if (currentSnippet != null && currentSnippetStepText != null) {
								snippetsMap.put(currentSnippetStepText, currentSnippet.toString());
							}
							
							// Start new snippet
							currentSnippetStepText = snippetMatcher.group(2).trim();
							currentSnippet = new StringBuilder();
							currentSnippet.append(line).append("\n");
						} else if (currentSnippet != null && !line.trim().isEmpty()) {
							// Continue building current snippet
							currentSnippet.append(line).append("\n");
						} else if (line.trim().isEmpty() && currentSnippet != null) {
							// Empty line marks end of snippet
							if (currentSnippetStepText != null) {
								snippetsMap.put(currentSnippetStepText, currentSnippet.toString());
							}
							currentSnippet = null;
							currentSnippetStepText = null;
						}
						continue;
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
				
				// Save last snippet if exists
				if (currentSnippet != null && currentSnippetStepText != null) {
					snippetsMap.put(currentSnippetStepText, currentSnippet.toString());
				}
			}

			// Wait for process to complete
			int exitCode = process.waitFor();

			// Store matched steps
			matchedSteps = new ArrayList<>(stepMatchMap.values());

			// Build map of unmatched steps with their snippets
			Map<Integer, Collection<String>> unmatchedStepsWithSnippets = new HashMap<>();
			editorDocument.getSteps().forEach(step -> {
				int lineNumber = step.getLocation().getLine().intValue();
				if (!stepMatchMap.containsKey(lineNumber)) {
					String stepText = step.getText();
					
					// Try to find matching snippet for this step text
					List<String> snippets = new ArrayList<>();
					if (snippetsMap.containsKey(stepText)) {
						snippets.add(snippetsMap.get(stepText));
					}
					
					unmatchedStepsWithSnippets.put(lineNumber, snippets);
				}
			});
			
			// Create markers for unmatched steps with snippets
			if (!unmatchedStepsWithSnippets.isEmpty()) {
				MarkerFactory.missingSteps(resource, unmatchedStepsWithSnippets, Activator.PLUGIN_ID, false);
			} else {
				// Delete any existing unmatched step markers if all steps are now matched
				BehaveMarkerFactory.unmatchedSteps(resource, Collections.emptyList(), Activator.PLUGIN_ID, false);
			}

			return Status.OK_STATUS;

		} catch (InterruptedException e) {
			// Return cancel status for interrupted exception
			return Status.CANCEL_STATUS;
		} catch (IOException e) {
			// Log the error but don't show error popup - create marker instead
			ILog.get().error("Behave validation failed - check that behave is installed and accessible", e);
			try {
				BehaveMarkerFactory.behaveExecutionError(resource, 
					"Failed to run behave for validation. Check that behave is installed and the behave command is configured correctly in preferences. See error log for details.");
			} catch (CoreException ce) {
				// Ignore marker creation failure
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public Collection<StepMatch> getMatchedSteps() {
		return matchedSteps;
	}
}
