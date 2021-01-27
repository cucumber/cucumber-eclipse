package io.cucumber.eclipse.java;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.RGB;

import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;
import io.cucumber.eclipse.java.steps.JavaStepDefinitionsProvider;

@SuppressWarnings("restriction")
public class JDTUtil {

	public JDTUtil() {
		// TODO Auto-generated constructor stub
	}

	public static IJavaProject getJavaProject(String projectName) {
		if ((projectName == null) || (projectName.length() < 1)) {
			return null;
		}
		return getJavaModel().getJavaProject(projectName);
	}

	public static IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static IJavaProject getJavaProject(IDocument document) throws CoreException {
		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
		if (buffer != null) {
			IPath location = buffer.getLocation();
			if (location != null) {
				return getJavaProject(ResourcesPlugin.getWorkspace().getRoot().getFile(location));
			}
		}
		return null;
	}

	public static IJavaProject getJavaProject(IResource resource) throws CoreException {
		if (resource != null) {
			IProject project = resource.getProject();
			if (isJavaProject(project)) {
				return JavaCore.create(project);
			}
		}
		return null;
	}

	public static boolean isJavaProject(IProject project) {
		try {
			return project != null && project.isOpen() && project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}

	public static URLClassLoader createClassloader(IJavaProject javaProject) throws CoreException {
		return createClassloader(javaProject, JDTUtil.class.getClassLoader());
	}

	public static URLClassLoader createClassloader(IJavaProject javaProject, ClassLoader parent) throws CoreException {
		String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
		List<URL> urlList = new ArrayList<URL>();
		for (String entry : classPathEntries) {
			try {
				if (entry.startsWith("file:/")) {
					urlList.add(new URL(entry));
				} else {
					urlList.add(new File(entry).toURI().toURL());
				}
			} catch (MalformedURLException e) {
				Activator.getDefault().getLog().error(
						"can't add classpathentry " + entry + " for project " + javaProject.getProject().getName(), e);
			}
		}
		URL[] urls = urlList.toArray(new URL[urlList.size()]);
		return new URLClassLoader(urls, new FilteringClassLoader(parent));
	}

	// TODO workaround for bug https://github.com/cucumber/cucumber-jvm/issues/2212
	private static final class FilteringClassLoader extends ClassLoader {
		public FilteringClassLoader(ClassLoader parent) {
			super("FilteringClassLoader", parent);
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			Spliterator<URL> spliterator = Spliterators.spliteratorUnknownSize(super.getResources(name).asIterator(),
					Spliterator.ORDERED);
			return Collections.enumeration(StreamSupport.stream(spliterator, false).filter(url -> {
				boolean equals = url.getProtocol().equals("bundleresource");
				if (equals) {
					System.out.println("Filtered " + url);
				}
				return !equals;
			}).collect(Collectors.toList()));
		}

	}

	public static IMethod[] resolveMethod(IJavaProject project, CucumberCodeLocation codeLocation,
			IProgressMonitor monitor) throws JavaModelException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, codeLocation.toString(), 100);
		String typeName = codeLocation.getTypeName();
		if (typeName.isBlank()) {
			return new IMethod[0];
		}
		return resolveTypeMethod(project.findType(typeName, subMonitor.split(10)), codeLocation, subMonitor.split(90));
	}

	public static IMethod[] resolveTypeMethod(IType type, CucumberCodeLocation codeLocation, IProgressMonitor monitor)
			throws JavaModelException {
		if (type != null) {
			String methodName = codeLocation.getMethodName();
			if (methodName.isBlank()) {
				return null;
			}
			IMethod[] candidates = Arrays.stream(type.getMethods())
					.filter(method -> method.getElementName().equals(methodName)).toArray(IMethod[]::new);
			if (candidates.length > 1) {
				// FIXME try to find match method parameters!
			}
			return candidates;
		}
		return new IMethod[0];
	}

	public static String getMethodName(IMethod method) throws JavaModelException {
		StringBuilder name = new StringBuilder();
		name.append(method.getDeclaringType().getElementName());
		name.append('.');
		name.append(method.getElementName());
		name.append("(");
		String.join(",", Arrays.stream(method.getParameterTypes()).map(Signature::toString).toArray(String[]::new));
		name.append(")");

		return name.toString();
	}

	public static String getJavadoc(IMethod method) {
		try {
			String content = JavadocContentAccess2.getHTMLContent(method, true);
			if (content != null) {
				StringBuilder buffer = new StringBuilder(content);
				ColorRegistry registry = JFaceResources.getColorRegistry();
				RGB fgRGB = registry.getRGB("org.eclipse.jdt.ui.Javadoc.foregroundColor"); //$NON-NLS-1$
				RGB bgRGB = registry.getRGB("org.eclipse.jdt.ui.Javadoc.backgroundColor"); //$NON-NLS-1$
				HTMLPrinter.insertPageProlog(buffer, 0, fgRGB, bgRGB, "");
				HTMLPrinter.addPageEpilog(buffer);
				return buffer.toString();
			}
		} catch (CoreException e) {
		}
		return null;
	}

	public static Collection<ICompilationUnit> getGlueSources(IJavaProject javaProject, IProgressMonitor monitor)
			throws CoreException {
		if (javaProject == null) {
			return Collections.emptyList();
		}
		List<ICompilationUnit> units = new ArrayList<>();
		findGlueSources(javaProject, units, new HashSet<>(), monitor);
		return units;
	}

	private static void findGlueSources(IJavaProject javaProject, List<ICompilationUnit> units,
			Set<String> analyzedProjects, IProgressMonitor monitor) throws CoreException {
		IPackageFragment[] fragments = javaProject.getPackageFragments();
		SubMonitor subMonitor = SubMonitor.convert(monitor, units.size() * 100 + fragments.length * 100);
		for (IPackageFragment fragment : fragments) {
			ICompilationUnit[] compilationUnits = fragment.getCompilationUnits();
			SubMonitor subSub = subMonitor.split(100);
			subSub.setWorkRemaining(compilationUnits.length);
			for (ICompilationUnit unit : compilationUnits) {
				if (hasCucumberGlueAnnotation(unit, subSub.split(1))) {
					units.add(unit);
				}
			}
		}
		IJavaProject[] references = Arrays.stream(javaProject.getProject().getReferencedProjects()).map(project -> {
			if (analyzedProjects.add(project.getName())) {
				try {
					return JDTUtil.getJavaProject(project);
				} catch (CoreException e) {
				}
			}
			return null;
		}).filter(Objects::nonNull).toArray(IJavaProject[]::new);
		for (IJavaProject reference : references) {
			findGlueSources(reference, units, analyzedProjects, subMonitor.split(100));
		}
	}

	public static boolean hasCucumberGlueAnnotation(ICompilationUnit compilationUnit, IProgressMonitor monitor)
			throws JavaModelException {
		IImportDeclaration[] allimports = compilationUnit.getImports();

		for (IImportDeclaration decl : allimports) {
			Matcher m = JavaStepDefinitionsProvider.ioCucumberAnnotationMatcher.matcher(decl.getElementName());
			if (m.find()) {
				return true;
			}

		}
		return false;
	}

}
