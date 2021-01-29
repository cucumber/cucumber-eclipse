package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import io.cucumber.core.plugin.ProgressFormatter;
import io.cucumber.core.plugin.UsageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.eclipse.editor.console.CucumberConsole;
import io.cucumber.eclipse.editor.console.CucumberConsoleFactory;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberStepParserPlugin;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.plugin.Plugin;
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
	public Stream<Envelope> launch(Map<GherkinEditorDocument, IStructuredSelection> selection, Mode mode,
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
		for (Entry<IJavaProject, List<Entry<GherkinEditorDocument, IStructuredSelection>>> entry : projectMap
				.entrySet()) {
			IJavaProject project = entry.getKey();
			Map<GherkinEditorDocument, IStructuredSelection> map = entry.getValue().stream()
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			List<Feature> list = map.keySet().stream().map(DocumentResource::new)
					.map(CucumberRuntime::loadFeature).flatMap(Optional::stream).collect(Collectors.toList());
			runFeatures(project, list, CucumberConsoleFactory.getConsole(true), subMonitor.split(100));

		}
		return Stream.empty();
	}

	@Override
	public boolean supports(IResource resource) {
		return JDTUtil.isJavaProject(resource.getProject());
	}

	@Override
	public boolean supports(Mode mode) {
		return mode == Mode.DEBUG || mode == Mode.RUN;
	}

	protected void runFeatures(IJavaProject javaProject, List<Feature> features, CucumberConsole console,
			IProgressMonitor monitor) throws CoreException {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		URLClassLoader classloader = JDTUtil.createClassloader(javaProject);
		try {
			Thread.currentThread().setContextClassLoader(classloader);
			// TODO configure add plugins for output progres......
			RuntimeOptionsBuilder runtimeOptions = new RuntimeOptionsBuilder()//
					.addDefaultGlueIfAbsent()//
					.setThreads(java.lang.Runtime.getRuntime().availableProcessors())//
					.setMonochrome(!AnsiConsolePreferenceUtils.isAnsiConsoleEnabled())
					.addDefaultSummaryPrinterIfAbsent()//
			;

			CucumberStepParserPlugin stepParserPlugin = new CucumberStepParserPlugin();
			try (IOConsoleOutputStream stream = console.newOutputStream()) {
				// FIXME workaround for https://github.com/cucumber/cucumber-jvm/issues/2216
				PrintStream old = System.out;
				System.setOut(new PrintStream(stream));
				try {
					Plugin[] plugins = new Plugin[] { new ProgressFormatter(stream),
							new UsageFormatter(console.newOutputStream()) };
					final Runtime runtime = Runtime.builder()//
							.withRuntimeOptions(runtimeOptions.build())//
							.withClassLoader(() -> classloader)//
							.withAdditionalPlugins(plugins)
							.withFeatureSupplier(() -> Collections.unmodifiableList(features))//
							.withAdditionalPlugins(stepParserPlugin)//
							.build();
					runtime.run();
				} finally {
					System.setOut(old);
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, getClass(), "running cucumber failed", e));
			}
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
			try {
				classloader.close();
			} catch (IOException e) {
				Activator.getDefault().getLog().warn("Closing classloader failed", e);
			}
		}

	}

}
