package io.cucumber.eclipse.java.validation;

import static io.cucumber.eclipse.editor.Tracing.PERFORMANCE_STEPS;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.plugins.CucumberMatchedStepsPlugin;
import io.cucumber.eclipse.java.plugins.CucumberMissingStepsPlugin;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.CucumberStepParserPlugin;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferences;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.eclipse.java.validation.JavaGlueValidatorService.GlueSteps;
import io.cucumber.plugin.Plugin;

/**
 * Utility class for validating Cucumber glue code in Java projects.
 * <p>
 * This class provides the core validation logic that runs Cucumber in dry-run mode
 * to match Gherkin steps with their corresponding Java step definitions.
 * </p>
 */
final class JavaGlueJob {

	private JavaGlueJob() {
		// Utility class - prevent instantiation
	}

	/**
	 * Validates glue code for multiple documents within the context of a Java project.
	 * All documents are processed in a single Cucumber runtime for efficiency.
	 * 
	 * @param editorDocuments the documents to validate
	 * @param javaProject the Java project containing the glue code
	 * @param projectPreferences the project-specific Cucumber preferences
	 * @param validationPlugins set of validation plugin class names to use for all documents
	 * @param monitor the progress monitor for cancellation
	 * @return Map of documents to GlueSteps containing matched and available steps
	 */
	static Map<GherkinEditorDocument, GlueSteps> validateGlue(Collection<GherkinEditorDocument> editorDocuments, 
			IJavaProject javaProject, CucumberJavaPreferences projectPreferences,
			Set<String> validationPlugins, IProgressMonitor monitor) {
		
		Map<GherkinEditorDocument, GlueSteps> resultsByDocument = new HashMap<>();
		
		if (editorDocuments.isEmpty()) {
			return resultsByDocument;
		}
		
		long start = System.currentTimeMillis();
		DebugTrace debug = Tracing.get();
		
		try (CucumberRuntime rt = CucumberRuntime.create(javaProject)) {
			rt.setGenerator(new IncrementingUuidGenerator());
			RuntimeOptionsBuilder runtimeOptions = rt.getRuntimeOptions();
			runtimeOptions.setDryRun();
			
			// Add all features to the runtime and map them by URI
			Map<URI, GherkinEditorDocument> documentsByUri = new HashMap<>();
			for (GherkinEditorDocument editorDocument : editorDocuments) {
				if (monitor.isCanceled()) {
					break;
				}
				
				IResource resource = editorDocument.getResource();
				monitor.subTask(resource.getName());
				debug.traceEntry(PERFORMANCE_STEPS, resource);
				
				try {
					Optional<Feature> feature = rt.addFeature(editorDocument);
					if (feature.isPresent()) {
						URI featureUri = feature.get().getUri();
						documentsByUri.put(featureUri, editorDocument);
					}
				} catch (FeatureParserException e) {
					// the feature has syntax errors, we can't check the glue then...
					continue;
				}
			}
			
			if (documentsByUri.isEmpty()) {
				return resultsByDocument;
			}
			
			addGlueOptions(runtimeOptions, projectPreferences);
			
			CucumberMissingStepsPlugin missingStepsPlugin = new CucumberMissingStepsPlugin();
			CucumberStepParserPlugin stepParserPlugin = new CucumberStepParserPlugin();
			CucumberMatchedStepsPlugin matchedStepsPlugin = new CucumberMatchedStepsPlugin();
			rt.addPlugin(stepParserPlugin);
			rt.addPlugin(matchedStepsPlugin);
			rt.addPlugin(missingStepsPlugin);
			
			// Add validation plugins
			Map<String, Plugin> validationPluginInstances = new HashMap<>();
			for (String pluginClass : validationPlugins) {
				Plugin classpathPlugin = rt.addPluginFromClasspath(pluginClass);
				if (classpathPlugin != null) {
					validationPluginInstances.put(pluginClass, classpathPlugin);
				}
			}
			
			try {
				rt.run(monitor);
				
				// Process results for each document
				Collection<CucumberStepDefinition> steps = stepParserPlugin.getStepList();
				
				for (Map.Entry<URI, GherkinEditorDocument> entry : documentsByUri.entrySet()) {
					if (monitor.isCanceled()) {
						break;
					}
					
					URI uri = entry.getKey();
					GherkinEditorDocument document = entry.getValue();
					IResource resource = document.getResource();
					
					// Get feature-specific results
					Collection<MatchedStep<?>> matchedSteps = matchedStepsPlugin.getMatchedStepsForFeature(uri);
					Map<Integer, Collection<String>> snippets = missingStepsPlugin.getSnippetsForFeature(uri);
					
					// Collect validation errors from plugins
					Map<Integer, String> validationErrors = new HashMap<>();
					for (Plugin plugin : validationPluginInstances.values()) {
						addErrors(plugin, validationErrors);
					}
					
					// Create markers for this document
					MarkerFactory.validationErrorOnStepDefinition(resource, validationErrors, false);
					MarkerFactory.missingSteps(resource, snippets, Activator.PLUGIN_ID, false);
					
					debug.traceExit(PERFORMANCE_STEPS,
							matchedSteps.size() + " step(s) /  " + steps.size() + " step(s)  matched, "
									+ snippets.size() + " snippet(s) where suggested");
					
					resultsByDocument.put(document, new GlueSteps(List.copyOf(steps), List.copyOf(matchedSteps)));
				}
				
				debug.trace(PERFORMANCE_STEPS, 
						"Total validation time for " + documentsByUri.size() + " document(s): " 
						+ (System.currentTimeMillis() - start) + "ms");
				
			} catch (Throwable e) {
				EditorLogging.error("Validate Glue-Code failed", e);
				// Create error markers for all documents
				for (GherkinEditorDocument document : documentsByUri.values()) {
					MarkerFactory.glueValidationError(document.getResource(),
							"Failed to validate step definitions. Check that your project is properly configured and dependencies are available. See error log for details.",
							"glue_validation_error");
				}
			}
		} catch (CoreException e) {
			EditorLogging.error("Failed to create Cucumber runtime", e);
		}
		
		return resultsByDocument;
	}

	static Set<String> extractValidationPlugins(GherkinEditorDocument editorDocument, 
			CucumberJavaPreferences projectPreferences) {
		Set<String> plugins = new LinkedHashSet<>();
		IDocument doc = editorDocument.getDocument();
		String documentContent = doc.get();
		String[] lines = documentContent.split("\\r?\\n");
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("#")) {
				String[] split = trimmed.split("validation-plugin:", 2);
				if (split.length == 2) {
					plugins.add(split[1].trim());
				}
			}
		}
		projectPreferences.plugins().forEach(plugins::add);
		return plugins;
	}

	@SuppressWarnings("unchecked")
	private static void addErrors(Plugin plugin, Map<Integer, String> validationErrors) {
		try {
			Method method = plugin.getClass().getMethod("getValidationErrors");
			Object invoke = method.invoke(plugin);
			if (invoke instanceof Map) {
				@SuppressWarnings("rawtypes")
				Map map = (Map) invoke;
				validationErrors.putAll(map);
			}
		} catch (Exception e) {
			EditorLogging.error("Failed to get validation errors from plugin: " + plugin.getClass().getName(), e);
		}
	}

	private static void addGlueOptions(RuntimeOptionsBuilder runtimeOptions,
			CucumberJavaPreferences projectPreferences) {
		projectPreferences.glueFilter().forEach(gluePath -> {
			gluePath = gluePath.trim();
			if (gluePath.endsWith("*")) {
				gluePath = gluePath.substring(0, gluePath.length() - 1);
			}
			if (gluePath.endsWith("/")) {
				gluePath = gluePath.substring(0, gluePath.length() - 1);
			}
			if (gluePath.endsWith(".")) {
				gluePath = gluePath.substring(0, gluePath.length() - 1);
			}
			try {
				runtimeOptions.addGlue(GluePath.parse(gluePath));
			} catch (RuntimeException e) {
				EditorLogging.error("Failed to parse glue path: " + gluePath, e);
			}
		});
	}
}