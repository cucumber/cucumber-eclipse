package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.osgi.service.component.annotations.Component;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.eclipse.editor.console.CucumberConsole;
import io.cucumber.eclipse.editor.console.CucumberConsoleFactory;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberEclipsePlugin;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.messages.Messages.Envelope;
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
	public void launch(Map<GherkinEditorDocument, IStructuredSelection> selection, Mode mode, boolean temporary,
			IProgressMonitor monitor) throws OperationCanceledException, CoreException {
		Map<IJavaProject, List<Entry<GherkinEditorDocument, IStructuredSelection>>> projectMap = selection.entrySet()
				.stream().filter(entry -> supports(entry.getKey().getResource()))
				.collect(Collectors.groupingBy(entry -> {
					try {
						return JDTUtil.getJavaProject(entry.getKey().getResource());
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
				}));
		SubMonitor subMonitor = SubMonitor.convert(monitor, projectMap.size() * 100);

		// FIXME choose between embedded runner and external runner!

		for (Entry<IJavaProject, List<Entry<GherkinEditorDocument, IStructuredSelection>>> entry : projectMap
				.entrySet()) {
			IJavaProject project = entry.getKey();
			Map<GherkinEditorDocument, IStructuredSelection> map = entry.getValue().stream()
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			List<Feature> list = map.keySet().stream().map(DocumentResource::new).map(CucumberRuntime::loadFeature)
					.flatMap(Optional::stream).collect(Collectors.toList());
			try (CucumberConsole console = CucumberConsoleFactory.getConsole(true)) {
				runFeaturesEmbedded(project, list, mode, console, subMonitor.split(100));
			}
		}
	}

	@Override
	public boolean supports(IResource resource) {
		return JDTUtil.isJavaProject(resource.getProject());
	}

	@Override
	public boolean supports(Mode mode) {
		return mode == Mode.DEBUG || mode == Mode.RUN || mode == Mode.PROFILE;
	}

	public static void runFeaturesEmbedded(IJavaProject javaProject, List<Feature> features, Mode mode,
			CucumberConsole console,
			IProgressMonitor monitor) throws CoreException {
		try (CucumberRuntime cucumberRuntime = CucumberRuntime.create(javaProject)) {
			CucumberEclipsePlugin plugin = new CucumberEclipsePlugin(new Consumer<Envelope>() {

				@Override
				public void accept(Envelope envelope) {
					// TODO publish it

				}
			});
			for (Feature feature : features) {
				cucumberRuntime.addFeature(feature);
			}
			RuntimeOptionsBuilder options = cucumberRuntime.getRuntimeOptions();
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

}
