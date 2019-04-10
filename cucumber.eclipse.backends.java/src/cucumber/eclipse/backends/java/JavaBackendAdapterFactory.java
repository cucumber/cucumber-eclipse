package cucumber.eclipse.backends.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.eclipse.backends.java.properties.JavaBackendPropertyPage;
import cucumber.eclipse.steps.integration.KeyWordProvider;
import cucumber.runtime.DefaultTypeRegistryConfiguration;
import cucumber.runtime.Reflections;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;

public class JavaBackendAdapterFactory implements IAdapterFactory, IResourceChangeListener {
	
	private static final JavaKeywordsProvider KEYWORDS_PROVIDER = new JavaKeywordsProvider();
	Map<IProject, ExpressionFactory> cache = new HashMap<>();

	public JavaBackendAdapterFactory() {
		JavaBackendActivator.addResourceChangeListener(this);
	}

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IProject) {
			IProject project = (IProject) adaptableObject;
			if (adapterType == KeyWordProvider.class) {
				//TODO should we check for any property here??
				return adapterType.cast(KEYWORDS_PROVIDER);
			}
			try {
				if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID) && JavaBackendPropertyPage.isBackendEnabled(project)) {
					
					if (adapterType == ExpressionFactory.class) {
						return adapterType.cast(getExpressionFactory(project));
					}
				}
			} catch (Throwable e) {
				JavaBackendActivator.getDefault().getLog().log(new Status(IStatus.ERROR, JavaBackendActivator.PLUGIN_ID,
						"can't adapt project " + project.getName() + " to type " + adapterType.getName(), e));
			}

		}
		return null;
	}

	private ExpressionFactory getExpressionFactory(IProject project) throws CoreException, MalformedURLException {
		synchronized (cache) {
			ExpressionFactory cachedRegistry = cache.get(project);
			if (cachedRegistry != null) {
				return cachedRegistry;
			}
			IJavaProject javaProject = JavaCore.create(project);
			URLClassLoader classLoader = getClassLoader(javaProject);
			MultiLoader multiLoader = new MultiLoader(classLoader);
			ResourceLoaderClassFinder classFinder = new ResourceLoaderClassFinder(multiLoader, classLoader);
			Reflections reflections = new Reflections(classFinder);
			List<String> gluePackages = new ArrayList<>();
			String glueString = JavaBackendPropertyPage.getGlueOption(project);
			if ("".equals(glueString)) {
				glueString = project.getName();
			}
			String[] split = glueString.split(",");
			for (String glue : split) {
				String trim = glue.trim();
				if (trim.isEmpty()) {
					continue;
				}
				gluePackages.add(trim);
				
			}
			if (gluePackages.isEmpty()) {
				return null;
			}
			//RuntimeOptions runtimeOptions = new RuntimeOptions("");
			TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(
					TypeRegistryConfigurer.class, gluePackages /*MultiLoader.packageName(runtimeOptions.getGlue())*/, new Class[0],
					new Object[0], new DefaultTypeRegistryConfiguration());
			TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
			typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
			ExpressionFactory expressionFactory = new ExpressionFactory(typeRegistry.parameterTypeRegistry());
			cache.put(project, expressionFactory);
			return expressionFactory;
		}

	}

	/**
	 * constructs a classloader out of the project
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 * @throws MalformedURLException
	 */
	private URLClassLoader getClassLoader(IJavaProject project) throws CoreException {
		String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(project);
		List<URL> urlList = new ArrayList<URL>();
		for (String entry : classPathEntries) {
			try {
				urlList.add(new File(entry).toURI().toURL());
			} catch (MalformedURLException e) {
				JavaBackendActivator.getDefault().getLog().log(new Status(IStatus.ERROR, JavaBackendActivator.PLUGIN_ID,
						"can't add classpathentry " + entry + " for project " + project.getProject().getName(), e));
			}
		}
		ClassLoader parentClassLoader = JavaBackendAdapterFactory.class.getClassLoader();
		URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
		URLClassLoader classLoader = new URLClassLoader(urls, parentClassLoader);
		return classLoader;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ExpressionFactory.class };
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		synchronized (cache) {
			for (Iterator<IProject> iterator = cache.keySet().iterator(); iterator.hasNext();) {
				IResourceDelta findMember = event.getDelta().findMember(iterator.next().getFullPath());
				if (findMember != null) {
					iterator.remove();
				}
			}
		}
	}

}
