package io.cucumber.eclipse.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import io.cucumber.eclipse.editor.steps.ExpressionDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinition;

/**
 * Step definition provider that scans the java classpath for step definitions
 * 
 * @author christoph
 *
 */
public class JavaClasspathStepDefinitionProvider extends JavaStepDefinitionsProvider {

	@Override
	public Collection<StepDefinition> findStepDefinitions(IResource stepDefinitionResource, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject project = getJavaProject(stepDefinitionResource);
		if (project != null) {
			return findStepDefinitionsInClasspath(project, monitor);
		}
		return Collections.emptyList();
	}

	private Set<StepDefinition> getCukeSteps(IMethod method) throws JavaModelException {
		Set<StepDefinition> stepDefinitions = new HashSet<StepDefinition>();
//		if (CucumberJavaPreferences.isUseStepDefinitionsFilters()) {
//			String[] filters = CucumberJavaPreferences.getStepDefinitionsFilters();
//			MethodStepDefinitionsPreferencesFilter filter = new MethodStepDefinitionsPreferencesFilter(filters);
//			if (!filter.accept(method)) {
//				// skip
//				return null;
//			}
//		}

		IAnnotation[] annotations = method.getAnnotations();
		for (IAnnotation annotation : annotations) {
			CucumberAnnotation cukeAnnotation = getCukeAnnotation(new ArrayList<CucumberAnnotation>(), annotation);
			if (cukeAnnotation != null) {
				IClassFile classFile = method.getClassFile();
				IJavaElement pkg = classFile.getParent();
				IJavaElement jar = pkg.getParent();
				ExpressionDefinition expression;
				expression = new ExpressionDefinition(getAnnotationText(annotation), cukeAnnotation.getLang());

				String classFileName = classFile.getElementName();
				String packageName = pkg.getElementName();
				String jarName = jar.getElementName();

				String label = String.format("%s > %s.%s#%s%s", jarName, packageName, classFileName,
						method.getElementName(), method.getSignature());


				StepDefinition step = new StepDefinition(method.getHandleIdentifier(), label, expression,
						jar.getResource(), StepDefinition.NO_LINE_NUMBER, method.getElementName(), packageName,
						getParameter(method));
				stepDefinitions.add(step);
			}
		}
		return stepDefinitions;
	}

	private Set<StepDefinition> findStepDefinitionsInClasspath(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		// FIXME use pattern io.cucumber.java*.
		SearchPattern searchPattern = SearchPattern.createPattern("*cucumber*.java*", IJavaSearchConstants.TYPE,
				IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE,
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE);

		try {
			SearchEngine engine = new SearchEngine();
			IJavaSearchScope[] scopes = this.computeScope(engine, javaProject, monitor);

			final Set<StepDefinition> stepDefinitions = new HashSet<StepDefinition>();
			SearchRequestor requestor = new SearchRequestor() {
				private long start, end;

				@Override
				public void beginReporting() {
					super.beginReporting();
					start = System.currentTimeMillis();
				}

				@Override
				public void endReporting() {
					super.endReporting();
					end = System.currentTimeMillis();
					System.out.println(
							"Search ended " + (end - start) + " ms - " + stepDefinitions.size() + " stepdefs found");
				}

				@Override
				public void acceptSearchMatch(SearchMatch match) {
					try {

						if (match.getAccuracy() == SearchMatch.A_ACCURATE) {

							if (match.getElement() instanceof IMethod) {
								IMethod method = (IMethod) match.getElement();
//								System.out.println("Method = " + method);
								Set<StepDefinition> methodStepDefinitions = getCukeSteps(method);
								if (methodStepDefinitions != null) {
									stepDefinitions.addAll(methodStepDefinitions);
								}

							} else if (match.getElement() instanceof IType) {
								IType resolvedType = (IType) match.getElement();
								IPackageFragment packageFragment = resolvedType.getPackageFragment();
//								System.out.println("Type = " + packageFragment.getElementName() + "."
//										+ resolvedType.getElementName());
								IMethod[] methods = resolvedType.getMethods();
								for (IMethod method : methods) {
									Set<StepDefinition> methodStepDefinitions = getCukeSteps(method);
									if (methodStepDefinitions != null) {
										stepDefinitions.addAll(methodStepDefinitions);
									}
								}
							}
						}

					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			};

			for (IJavaSearchScope scope : scopes) {
				jdtSearch(engine, searchPattern, scope, requestor, monitor);
			}

			return stepDefinitions;
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		} catch (CoreException e) {
			e.printStackTrace();
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	private IJavaSearchScope[] computeScope(SearchEngine searchEngine, IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		// TODO improve monitor support
		List<IJavaSearchScope> scope = new ArrayList<IJavaSearchScope>();
//		if (CucumberJavaPreferences.isUseStepDefinitionsFilters()) {
//			String[] filters = CucumberJavaPreferences.getStepDefinitionsFilters();
//			final List<IJavaElement> pkgScope = new ArrayList<IJavaElement>();
//			final List<IJavaElement> typeScope = new ArrayList<IJavaElement>();
//
//			IJavaSearchScope projectScope = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject });
//			SearchRequestor requestor = new SearchRequestor() {
//				@Override
//				public void acceptSearchMatch(SearchMatch match) throws CoreException {
//					if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
//						if (match.getElement() instanceof IPackageFragment) {
//							pkgScope.add((IJavaElement) match.getElement());
//						} else if (match.getElement() instanceof IType) {
//							typeScope.add((IJavaElement) match.getElement());
//						}
//					}
//				}
//			};
//
//			for (String filter : filters) {
//				if (filter.endsWith(".*")) {
//					// search for a package
//					String filterWithoutStar = filter.substring(0, filter.length() - 2);
//					SearchPattern searchPattern = SearchPattern.createPattern(filterWithoutStar,
//							IJavaSearchConstants.PACKAGE, IJavaSearchConstants.DECLARATIONS,
//							SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
//
//					searchEngine.search(searchPattern,
//							new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, projectScope,
//							requestor, monitor);
//				} else {
//					// search for a type
//					SearchPattern searchPattern = SearchPattern.createPattern(filter, IJavaSearchConstants.TYPE,
//							IJavaSearchConstants.DECLARATIONS,
//							SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
//
//					searchEngine.search(searchPattern,
//							new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, projectScope,
//							requestor, monitor);
//				}
//			}
//			if (!pkgScope.isEmpty()) {
//				scope.add(SearchEngine.createJavaSearchScope(pkgScope.toArray(new IJavaElement[pkgScope.size()]),
//						IJavaSearchScope.APPLICATION_LIBRARIES));
//			}
//			if (!typeScope.isEmpty()) {
//				scope.add(SearchEngine.createJavaSearchScope(typeScope.toArray(new IJavaElement[typeScope.size()]),
//						IJavaSearchScope.APPLICATION_LIBRARIES));
//			}
//
//		} else {
		scope.add(SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject },
				IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES
						| IJavaSearchScope.REFERENCED_PROJECTS));
//		}
		return scope.toArray(new IJavaSearchScope[scope.size()]);
	}

}
