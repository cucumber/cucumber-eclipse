package io.cucumber.eclipse.java.validation;

import static io.cucumber.eclipse.editor.Tracing.PERFORMANCE_STEPS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.GluePath;
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
	 * Validates glue code for a single document within the context of a Java project.
	 * 
	 * @param editorDocument the document to validate
	 * @param javaProject the Java project containing the glue code
	 * @param projectPreferences the project-specific Cucumber preferences
	 * @param monitor the progress monitor for cancellation
	 * @return GlueSteps containing matched and available steps, or null if validation failed
	 */
	static GlueSteps validateGlue(GherkinEditorDocument editorDocument, IJavaProject javaProject,
			CucumberJavaPreferences projectPreferences, IProgressMonitor monitor) {
		
		try {
			IResource resource = editorDocument.getResource();
			monitor.subTask(resource.getName());
			
			long start = System.currentTimeMillis();
			DebugTrace debug = Tracing.get();
			debug.traceEntry(PERFORMANCE_STEPS, resource);
			
			try (CucumberRuntime rt = CucumberRuntime.create(javaProject)) {
				rt.setGenerator(new IncrementingUuidGenerator());
				RuntimeOptionsBuilder runtimeOptions = rt.getRuntimeOptions();
				runtimeOptions.setDryRun();
				
				try {
					rt.addFeature(editorDocument);
				} catch (FeatureParserException e) {
					// the feature has syntax errors, we can't check the glue then...
					return null;
				}
				
				addGlueOptions(runtimeOptions, projectPreferences);
				
				CucumberMissingStepsPlugin missingStepsPlugin = new CucumberMissingStepsPlugin();
				CucumberStepParserPlugin stepParserPlugin = new CucumberStepParserPlugin();
				CucumberMatchedStepsPlugin matchedStepsPlugin = new CucumberMatchedStepsPlugin();
				rt.addPlugin(stepParserPlugin);
				rt.addPlugin(matchedStepsPlugin);
				rt.addPlugin(missingStepsPlugin);
				
				Collection<Plugin> validationPlugins = addValidationPlugins(editorDocument, rt, projectPreferences);
				
				try {
					rt.run(monitor);
					
					Map<Integer, String> validationErrors = new HashMap<>();
					for (Plugin plugin : validationPlugins) {
						addErrors(plugin, validationErrors);
					}
					
					Map<Integer, Collection<String>> snippets = missingStepsPlugin.getSnippets();
					MarkerFactory.validationErrorOnStepDefinition(resource, validationErrors, false);
					MarkerFactory.missingSteps(resource, snippets, Activator.PLUGIN_ID, false);
					
					Collection<CucumberStepDefinition> steps = stepParserPlugin.getStepList();
					Collection<MatchedStep<?>> matchedSteps = matchedStepsPlugin.getMatchedSteps();
					
					debug.traceExit(PERFORMANCE_STEPS,
							matchedSteps.size() + " step(s) /  " + steps.size() + " step(s)  matched, "
									+ snippets.size() + " snippet(s) where suggested || total run time "
									+ (System.currentTimeMillis() - start) + "ms)");
					
					return new GlueSteps(List.copyOf(steps), List.copyOf(matchedSteps));
					
				} catch (Throwable e) {
					EditorLogging.error("Validate Glue-Code failed", e);
					// Create an error marker to notify the user
					MarkerFactory.glueValidationError(resource,
							"Failed to validate step definitions. Check that your project is properly configured and dependencies are available. See error log for details.",
							"glue_validation_error");
					return null;
				}
			}
		} catch (CoreException e) {
			EditorLogging.error("Failed to create Cucumber runtime", e);
			return null;
		}
	}

	private static Collection<Plugin> addValidationPlugins(GherkinEditorDocument editorDocument, CucumberRuntime rt,
			CucumberJavaPreferences projectPreferences) {
		List<Plugin> validationPlugins = new ArrayList<>();
		IDocument doc = editorDocument.getDocument();
		Set<String> plugins = new LinkedHashSet<>();
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
		for (String plugin : plugins) {
			Plugin classpathPlugin = rt.addPluginFromClasspath(plugin);
			if (classpathPlugin != null) {
				validationPlugins.add(classpathPlugin);
			}
		}
		return validationPlugins;
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