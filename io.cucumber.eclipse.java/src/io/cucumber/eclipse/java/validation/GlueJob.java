package io.cucumber.eclipse.java.validation;

import static io.cucumber.eclipse.editor.Tracing.PERFORMANCE_STEPS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.cucumber.core.feature.GluePath;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberMatchedStepsPlugin;
import io.cucumber.eclipse.java.plugins.CucumberMissingStepsPlugin;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.CucumberStepParserPlugin;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferences;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.plugin.Plugin;

final class GlueJob extends Job {

	private GlueJob oldJob;
	private final IDocument document;
	private Runnable listenerRegistration;

	volatile Collection<MatchedStep<?>> matchedSteps;
	volatile Collection<CucumberStepDefinition> parsedSteps;

	public GlueJob(GlueJob oldJob, IDocument document) {
		super("Verify Cucumber Glue Code");
		this.oldJob = oldJob;
		this.document = document;
		if (oldJob != null) {
			this.matchedSteps = oldJob.matchedSteps;
			this.parsedSteps = oldJob.parsedSteps;
		} else {
			this.matchedSteps = Collections.emptySet();
			this.parsedSteps = Collections.emptySet();
		}
	}

	@Override
	protected void canceling() {
		disposeListener();
	}

	protected void disposeListener() {
		synchronized (this) {
			if (listenerRegistration != null) {
				listenerRegistration.run();
				listenerRegistration = () -> {
					// dummy to prevent further registration...
				};
			}
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (oldJob != null) {
			try {
				oldJob.join();
				oldJob = null;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Status.CANCEL_STATUS;
			}
		}
		GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
		if (editorDocument != null) {
			try {
				IResource resource = editorDocument.getResource();
				IJavaProject javaProject = JDTUtil.getJavaProject(resource);
				if (javaProject != null) {
					long start = System.currentTimeMillis();
					DebugTrace debug = Tracing.get();
					debug.traceEntry(PERFORMANCE_STEPS, resource);
					CucumberJavaPreferences projectProperties = getProperties(editorDocument);
					try (CucumberRuntime rt = CucumberRuntime.create(javaProject)) {
						RuntimeOptionsBuilder runtimeOptions = rt.getRuntimeOptions();
						runtimeOptions.setDryRun();
						try {
							rt.addFeature(editorDocument);
						} catch (FeatureParserException e) {
							// the feature has syntax errors, we can't check the glue then...
							return Status.CANCEL_STATUS;
						}
						addGlueOptions(runtimeOptions, projectProperties);
						CucumberMissingStepsPlugin missingStepsPlugin = new CucumberMissingStepsPlugin();
						CucumberStepParserPlugin stepParserPlugin = new CucumberStepParserPlugin();
						CucumberMatchedStepsPlugin matchedStepsPlugin = new CucumberMatchedStepsPlugin();
						rt.addPlugin(stepParserPlugin);
						rt.addPlugin(matchedStepsPlugin);
						rt.addPlugin(missingStepsPlugin);
						Collection<Plugin> validationPlugins = addValidationPlugins(editorDocument, rt,
								projectProperties);
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
							matchedSteps = Collections.unmodifiableCollection(matchedStepsPlugin.getMatchedSteps());
							parsedSteps = Collections.unmodifiableCollection(stepParserPlugin.getStepList());
							debug.traceExit(PERFORMANCE_STEPS,
									matchedSteps.size() + " step(s) /  " + steps.size() + " step(s)  matched, "
											+ snippets.size() + " snippet(s) where suggested || total run time "
											+ (System.currentTimeMillis() - start) + "ms)");
							if (!monitor.isCanceled()) {
								ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager()
										.getTextFileBuffer(document);
								if (buffer != null) {
//										IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//												.getActivePage().getActiveEditor();
//										if (editorPart instanceof ITextEditor) {
//											ITextEditor textEditor = (ITextEditor) editorPart;
//											if (textEditor.getDocumentProvider()
//													.getDocument(textEditor.getEditorInput()) == document) {
//												// FIXME how to get the Textbuffer?
//											}
//										}

								}
							}

						} catch (Throwable e) {
							// TODO
							ILog.get().error("Validate Glue-Code failed", e);
						}
					}
				}
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
//			jobMap.remove(document, this);
		// FIXME notify document reconsilers??
		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	private static Collection<Plugin> addValidationPlugins(GherkinEditorDocument editorDocument, CucumberRuntime rt,
			CucumberJavaPreferences projectProperties) {
		List<Plugin> validationPlugins = new ArrayList<>();
		IDocument doc = editorDocument.getDocument();
		int lines = doc.getNumberOfLines();
		Set<String> plugins = new LinkedHashSet<>();
		for (int i = 0; i < lines; i++) {
			try {
				IRegion firstLine = doc.getLineInformation(i);
				String line = doc.get(firstLine.getOffset(), firstLine.getLength()).trim();
				if (line.startsWith("#")) {
					String[] split = line.split("validation-plugin:", 2);
					if (split.length == 2) {
						plugins.add(split[1].trim());
					}
				}
			} catch (BadLocationException e) {
			}
		}
		projectProperties.plugins().forEach(plugins::add);
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
			e.printStackTrace();
		}

	}

	private CucumberJavaPreferences getProperties(GherkinEditorDocument editorDocument) {
		IResource resource = editorDocument.getResource();
		CucumberJavaPreferences projectProperties = CucumberJavaPreferences.of(resource);
		if (resource != null) {
			synchronized (this) {
				if (listenerRegistration == null) {
					IEclipsePreferences node = projectProperties.node();
					List<Runnable> list = new ArrayList<>();
					if (node != null) {
						IPreferenceChangeListener listener = new IPreferenceChangeListener() {

							@Override
							public void preferenceChange(PreferenceChangeEvent event) {
								schedule();
							}
						};
						node.addPreferenceChangeListener(listener);
						list.add(() -> node.removePreferenceChangeListener(listener));
					}
					IPreferenceStore store = projectProperties.store();
					if (store != null) {
						IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent event) {
								schedule();
							}
						};
						store.addPropertyChangeListener(propertyChangeListener);
					}
					listenerRegistration = () -> list.forEach(Runnable::run);
				}
			}
		}
		return projectProperties;
	}

	private static void addGlueOptions(RuntimeOptionsBuilder runtimeOptions,
			CucumberJavaPreferences projectProperties) {
		projectProperties.glueFilter().forEach(gluePath -> {
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
				e.printStackTrace();
			}
		});
	}
}