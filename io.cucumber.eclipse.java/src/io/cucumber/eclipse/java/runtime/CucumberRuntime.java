package io.cucumber.eclipse.java.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.messages.GherkinMessagesFeatureParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.resource.Resource;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.launching.FileResource;
import io.cucumber.java.JavaBackendProviderService;
import io.cucumber.plugin.Plugin;

/**
 * Accessor to the cucumber-jvm runtime
 * 
 * @author christoph
 *
 */
public final class CucumberRuntime implements AutoCloseable {

	private static final FeatureParser FEATURE_PARSER = new FeatureParser(UUID::randomUUID);

	private static final JavaBackendProviderService BACKEND_PROVIDER_SERVICE = new JavaBackendProviderService();

	private List<Feature> features = new ArrayList<>();

	private List<Plugin> plugins = new ArrayList<>();

	private IJavaProject javaProject;

	private URLClassLoader classLoader;

	private RuntimeOptionsBuilder runtimeOptions;

	private CucumberRuntime(IJavaProject javaProject) throws CoreException {
		this.javaProject = javaProject;
		this.classLoader = JDTUtil.createClassloader(javaProject);
		runtimeOptions = new RuntimeOptionsBuilder()//
				.addDefaultGlueIfAbsent()//
				.setThreads(java.lang.Runtime.getRuntime().availableProcessors())//
				.setSnippetType(SnippetType.CAMELCASE)//
				.setMonochrome(true);
	}

	public static Optional<Feature> loadFeature(Resource resource) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(GherkinMessagesFeatureParser.class.getClassLoader());
			return FEATURE_PARSER.parseResource(resource);
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}
	}

	@Override
	public void close() {
		try {
			classLoader.close();
		} catch (IOException e) {
			Activator.warn("can't close classlaoder for project " + javaProject.getElementName(), e);
		}
	}

	public RuntimeOptionsBuilder getRuntimeOptions() {
		return runtimeOptions;
	}

	public void run(IProgressMonitor monitor) {
		run(monitor, null);
	}

	public void run(IProgressMonitor monitor, PrintStream stream) {

		// TODO progressmonitor plugin!?

		RuntimeOptions options = runtimeOptions.build();
		PrintStream old = System.out;
		try {
			if (stream != null) {
				System.setOut(stream);
			}
			final Runtime runtime = Runtime.builder()//
					.withRuntimeOptions(options)//
					.withClassLoader(() -> classLoader)//
					.withFeatureSupplier(() -> Collections.unmodifiableList(features))//
					.withAdditionalPlugins(plugins.toArray(Plugin[]::new))//
					.withBackendSupplier(new BackendSupplier() {

						@Override
						public Collection<? extends Backend> get() {
							// TODO https://github.com/cucumber/cucumber-jvm/issues/2217
							ThreadLocalObjectFactorySupplier supplier = new ThreadLocalObjectFactorySupplier(
									new ObjectFactoryServiceLoader(options));
							ObjectFactory objectFactory = supplier.get();
							Set<Backend> backends = Collections.singleton(
									BACKEND_PROVIDER_SERVICE.create(objectFactory, objectFactory, () -> classLoader));
							return backends;
						}
					}).build();
			// FIXME workaround for https://github.com/cucumber/cucumber-jvm/issues/2216
			runtime.run();
		} finally {
			if (stream != null) {
				System.setOut(old);
			}
		}
	}

	public void addFeature(IFile file) {
		loadFeature(new FileResource(file)).ifPresent(features::add);
	}

	public void addFeature(Feature feature) {
		features.add(feature);
	}

	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
	}

	public void addFeature(GherkinEditorDocument document) {
		IResource resource = document.getResource();
		URI uri = Objects.requireNonNullElseGet(resource.getLocationURI(), () -> resource.getRawLocationURI());
		// TODO can we get any information about an error here??
		// TODO it would be good if the featureparser would be capable of loading
		// directly a GherkingDocument
		loadFeature(new Resource() {

			@Override
			public URI getUri() {
				return uri;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(document.getDocument().get().getBytes(StandardCharsets.UTF_8));
			}
		}).ifPresent(features::add);
	}

	public static CucumberRuntime create(IJavaProject javaProject) throws CoreException {
		// TODO can we cache the classlaoder here to optimize performance? As long as
		// the classpath do not change there won't be any new classes be loaded...
		return new CucumberRuntime(javaProject);
	}

}
