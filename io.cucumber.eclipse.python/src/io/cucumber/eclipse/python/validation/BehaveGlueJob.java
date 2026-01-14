package io.cucumber.eclipse.python.validation;

import static io.cucumber.eclipse.editor.Tracing.PERFORMANCE_STEPS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.python.Activator;
import io.cucumber.eclipse.python.launching.BehaveProcessLauncher;
import io.cucumber.eclipse.python.preferences.BehavePreferences;
import io.cucumber.eclipse.python.validation.BehaveGlueValidatorService.GlueSteps;

/**
 * Utility class for validating Behave glue code in Python projects.
 * <p>
 * This class provides the core validation logic that runs behave in dry-run mode
 * to match Gherkin steps with their corresponding Python step definitions.
 * </p>
 */
final class BehaveGlueJob {

	// Pattern to match step definition lines like:
	// @given('I have a calculator')             # calculator_steps.py:19
	private static final Pattern STEP_DEF_PATTERN = Pattern
			.compile("@\\w+\\('(.+?)'\\)\\s+#\\s+(.+?):(\\d+)");

	// Pattern to match step usage lines like:
	//   Given I have a calculator               # ../calculator.feature:7
	private static final Pattern STEP_USAGE_PATTERN = Pattern
			.compile("\\s+(Given|When|Then|And|But)\\s+(.+?)\\s+#\\s+(.+?):(\\d+)");

	private BehaveGlueJob() {
		// Utility class - prevent instantiation
	}

	/**
	 * Validates glue code for multiple documents within the context of a Python project.
	 * All documents are processed in a single behave process for efficiency.
	 * 
	 * @param editorDocuments the documents to validate
	 * @param project the project containing the feature files
	 * @param projectPreferences the project-specific Behave preferences
	 * @param monitor the progress monitor for cancellation
	 * @return Map of documents to GlueSteps containing matched steps
	 */
	static Map<GherkinEditorDocument, GlueSteps> validateGlue(Collection<GherkinEditorDocument> editorDocuments, 
			IProject project, BehavePreferences projectPreferences, IProgressMonitor monitor) {
		
		Map<GherkinEditorDocument, GlueSteps> resultsByDocument = new HashMap<>();
		
		if (editorDocuments.isEmpty()) {
			return resultsByDocument;
		}
		
		long start = System.currentTimeMillis();
		DebugTrace debug = Tracing.get();
		
		try {
			String behaveCommand = projectPreferences.behaveCommand();
			String workingDir = project.getLocation().toOSString();
			
			// Collect all feature paths and map them by filename
			List<String> featurePaths = new ArrayList<>();
			Map<String, GherkinEditorDocument> documentsByFeatureName = new HashMap<>();
			
			for (GherkinEditorDocument editorDocument : editorDocuments) {
				if (monitor.isCanceled()) {
					break;
				}
				
				IResource resource = editorDocument.getResource();
				if (resource == null) {
					continue;
				}
				
				monitor.subTask(resource.getName());
				debug.traceEntry(PERFORMANCE_STEPS, resource);
				
				String featurePath = resource.getLocation().toOSString();
				featurePaths.add(featurePath);
				documentsByFeatureName.put(resource.getName(), editorDocument);
			}
			
			if (featurePaths.isEmpty()) {
				return resultsByDocument;
			}
			
			// Build behave command with all feature files
			BehaveProcessLauncher launcher = new BehaveProcessLauncher()
				.withCommand(behaveCommand)
				.withWorkingDirectory(workingDir)
				.withDryRun(true)
				.withFormat("steps.usage")
				.withNoSummary(true);
			
			// Add all feature paths
			for (String path : featurePaths) {
				launcher.withFeaturePath(path);
			}

			Process process = launcher.launch();

			// Parse the output and group matches by feature file
			Map<String, Map<Integer, StepMatch>> matchesByFeature = new HashMap<>();
			Map<String, List<Integer>> unmatchedByFeature = new HashMap<>();
			
			// Initialize collections for all documents
			for (String featureName : documentsByFeatureName.keySet()) {
				matchesByFeature.put(featureName, new HashMap<>());
				unmatchedByFeature.put(featureName, new ArrayList<>());
			}
			
			String currentStepPattern = null;
			String currentStepFile = null;
			int currentStepLine = -1;

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (monitor.isCanceled()) {
						process.destroy();
						return resultsByDocument;
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
					if (usageMatcher.find()) {
						String stepText = usageMatcher.group(2);
						String featureFile = usageMatcher.group(3);
						int featureLine = Integer.parseInt(usageMatcher.group(4));

						// Extract just the filename from the path
						String featureName = extractFilename(featureFile);
						
						// Check if this is one of our documents
						if (documentsByFeatureName.containsKey(featureName)) {
							Map<Integer, StepMatch> featureMatches = matchesByFeature.get(featureName);
							
							if (currentStepPattern != null && currentStepFile != null) {
								// Matched step
								StepMatch match = new StepMatch(featureLine, stepText, currentStepFile, 
										currentStepLine, currentStepPattern);
								featureMatches.put(featureLine, match);
							} else {
								// Unmatched step (from UNDEFINED STEPS section)
								unmatchedByFeature.get(featureName).add(featureLine);
							}
						}
					}
				}
			}

			// Wait for process to complete
			process.waitFor();
			
			// Process results for each document
			for (Map.Entry<String, GherkinEditorDocument> entry : documentsByFeatureName.entrySet()) {
				if (monitor.isCanceled()) {
					break;
				}
				
				String featureName = entry.getKey();
				GherkinEditorDocument document = entry.getValue();
				IResource resource = document.getResource();
				
				Map<Integer, StepMatch> stepMatchMap = matchesByFeature.get(featureName);
				Collection<StepMatch> matchedSteps = stepMatchMap.values();
				
				// Find unmatched steps by comparing with all steps in document
				List<Integer> unmatchedLineNumbers = new ArrayList<>();
				document.getSteps().forEach(step -> {
					int lineNumber = step.getLocation().getLine().intValue();
					if (!stepMatchMap.containsKey(lineNumber)) {
						unmatchedLineNumbers.add(lineNumber);
					}
				});
				
				// Create markers for this document
				BehaveMarkerFactory.unmatchedSteps(resource, unmatchedLineNumbers, Activator.PLUGIN_ID, false);
				
				debug.traceExit(PERFORMANCE_STEPS,
						matchedSteps.size() + " step(s) matched, " + unmatchedLineNumbers.size() + " unmatched");
				
				resultsByDocument.put(document, new GlueSteps(List.copyOf(matchedSteps)));
			}
			
			debug.trace(PERFORMANCE_STEPS, 
					"Total validation time for " + documentsByFeatureName.size() + " document(s): " 
					+ (System.currentTimeMillis() - start) + "ms");

		} catch (InterruptedException e) {
			// Restore interrupt status
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			EditorLogging.error("Behave validation failed", e);
			// Create error markers for all documents
			for (GherkinEditorDocument document : editorDocuments) {
				MarkerFactory.glueValidationError(document.getResource(),
						"Failed to validate step definitions. Check that behave is installed and accessible. See error log for details.",
						"glue_validation_error");
			}
		} catch (CoreException e) {
			EditorLogging.error("Failed to create markers", e);
		}
		
		return resultsByDocument;
	}

	private static String extractFilename(String path) {
		// Extract filename from path (handles both forward and back slashes)
		int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		if (lastSlash >= 0) {
			return path.substring(lastSlash + 1);
		}
		return path;
	}
}
