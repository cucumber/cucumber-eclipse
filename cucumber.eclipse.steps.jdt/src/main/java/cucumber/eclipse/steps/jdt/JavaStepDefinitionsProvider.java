package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import cucumber.eclipse.steps.integration.AbstractStepDefinitionsProvider;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;
import cucumber.eclipse.steps.jdt.filter.CompilationUnitStepDefinitionsPreferencesFilter;
import cucumber.eclipse.steps.jdt.filter.MethodStepDefinitionsPreferencesFilter;
import cucumber.eclipse.steps.jdt.ui.CucumberJavaPreferences;
import io.cucumber.cucumberexpressions.CucumberExpressionException;

/**
 * Find step definitions on Java elements.
 * 
 * @author qvdk
 *
 */
public class JavaStepDefinitionsProvider extends AbstractStepDefinitionsProvider {

	protected static JavaStepDefinitionsProvider INSTANCE = new JavaStepDefinitionsProvider();

	private final Pattern cukeAnnotationMatcher = Pattern.compile("cucumber\\.api\\.java\\.([a-z_]+)\\.(.*)$");
	private static final String CUCUMBER_API_JAVA = "cucumber.api.java.";
	private static final String CUCUMBER_API_JAVA8 = "cucumber.api.java8.";
	private static final String REGEX_JAVA8_CUKEAPI = "cucumber\\.api\\.java8\\.(.*)";

	// secure usage of the singleton
	private JavaStepDefinitionsProvider() {
	}

	/**
	 * Initialize
	 * 
	 * @return StepDefinitions
	 */
	protected static JavaStepDefinitionsProvider getInstance() {
		return INSTANCE;
	}

	// From Java-Source-File(.java) : Collect All Steps as List based on
	// Cucumber-Annotations
	private List<StepDefinition> getCukeSteps(ICompilationUnit iCompUnit, MarkerFactory markerFactory,
			IProgressMonitor progressMonitor) throws JavaModelException, CoreException {

		long start = System.currentTimeMillis();

		List<StepDefinition> steps = new ArrayList<StepDefinition>();
		List<CucumberAnnotation> importedAnnotations = new ArrayList<CucumberAnnotation>();
		IImportDeclaration[] allimports = iCompUnit.getImports();

		for (IImportDeclaration decl : allimports) {

			// Match Package name
			Matcher m = cukeAnnotationMatcher.matcher(decl.getElementName());
			if (m.find()) {
				if ("*".equals(m.group(2))) {
					importedAnnotations.addAll(getAllAnnotationsInPackage(iCompUnit.getJavaProject(),
							CUCUMBER_API_JAVA + m.group(1), m.group(1)));
				} else {
					importedAnnotations.add(new CucumberAnnotation(m.group(2), m.group(1)));
				}
			}

			// If import declaration matches with 'cucumber.api.java8'
			// Then set Language of Java8-cuke-api
			if (decl.getElementName().matches(REGEX_JAVA8_CUKEAPI)) {
				String importDeclaration = decl.getElementName();
//				setJava8CukeLang(importDeclaration);
			}
		}

		List<MethodDeclaration> methodDeclList = null;
		JavaParser javaParser = null;
		for (IType t : iCompUnit.getTypes()) {
			// collect all steps from java8 lamdas
			for (IType ifType : t.newTypeHierarchy(progressMonitor).getAllInterfaces()) {

				if (ifType.isInterface() && ifType.getFullyQualifiedName().startsWith(CUCUMBER_API_JAVA8)) {
					String[] superInterfaceNames = ifType.getSuperInterfaceNames();
					for (String superIfName : superInterfaceNames) {
						if (superIfName.endsWith(".LambdaGlueBase")) {
							// we found a possible interface, now try to load the language...
//							String lang = ifType.getElementName().toLowerCase();
							// init if not done in previous step..
							if (javaParser == null) {
								javaParser = new JavaParser(iCompUnit, progressMonitor);
							}
							if (methodDeclList == null) {
								methodDeclList = javaParser.getAllMethods();
							}
							Set<String> keyWords = new HashSet<String>();
							for (IMethod method : ifType.getMethods()) {
								keyWords.add(method.getElementName());
							}
							List<MethodDefinition> methodDefList = new ArrayList<MethodDefinition>();
							// Visiting Methods/Constructors
							for (MethodDeclaration method : methodDeclList) {

								// Get Method/Constructor-Block{...}
								if (isCukeLambdaExpr(method, keyWords)) {
									// Collect method-body as List of Statements
									@SuppressWarnings("unchecked")
									List<Statement> statementList = method.getBody().statements();
									if (!statementList.isEmpty()) {
										MethodDefinition definition = new MethodDefinition(method.getName(),
												method.getReturnType2(), statementList);
										methodDefList.add(definition);
//										definition.setJava8CukeLang(lang);
									}
								}
							}
							// Iterate MethodDefinition
							for (MethodDefinition method : methodDefList) {
								// Iterate Method-Statements
								for (Statement statement : method.getMethodBodyList()) {
									// Add all lambda-steps to Step
									StepDefinition step = new StepDefinition();
									step.setSource(iCompUnit.getResource()); // source
									String lambdaStep = method.getLambdaStep(statement, keyWords);
									if (lambdaStep == null) {
										continue;
									}
									int lineNumber = javaParser.getLineNumber(statement);
									try {
										step.setText(lambdaStep); // step
										step.setLineNumber(lineNumber); // line-number
										step.setLang(method.getCukeLang()); // Language
										steps.add(step);
									} catch (CucumberExpressionException e) {
										markerFactory.syntaxErrorOnStepDefinition(iCompUnit.getResource(), e,
												lineNumber);
									}

								}
							}
						}
					}
				}
			}
			// Collect all steps from Annotations used in the methods as per imported
			// Annotations
			for (IMethod method : t.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {
					CucumberAnnotation cukeAnnotation = getCukeAnnotation(importedAnnotations, annotation);
					if (cukeAnnotation != null) {
						int lineNumber = getLineNumber(iCompUnit, annotation);
						StepDefinition step = new StepDefinition();
						step.setSource(method.getResource());
						step.setLineNumber(lineNumber);
						step.setLang(cukeAnnotation.getLang());
						steps.add(step);
						try {
							step.setText(getAnnotationText(annotation));
						} catch (CucumberExpressionException e) {
							markerFactory.syntaxErrorOnStepDefinition(iCompUnit.getResource(), e, lineNumber);
						}
					}
				}

			}
		}

		long end = System.currentTimeMillis();
//		System.out.println("getCukeSteps " + iCompUnit.getJavaProject().getElementName() + ": " + iCompUnit.getElementName() + " " + (end - start) + " ms.");

		return steps;
	}

	/**
	 * @param method
	 * @param i18n
	 * @return boolean
	 */
	private boolean isCukeLambdaExpr(MethodDeclaration method, Set<String> keywords) {
		@SuppressWarnings("unchecked")
		List<Statement> statements = method.getBody().statements();
		for (Statement statement : statements) {
			if (statement instanceof ExpressionStatement) {
				ExpressionStatement expressionStatement = (ExpressionStatement) statement;
				Expression expression = expressionStatement.getExpression();
				if (expression instanceof MethodInvocation) {
					String identifier = ((MethodInvocation) expression).getName().getIdentifier();
					if (keywords.contains(identifier)) {
						// we found a lamda
						return true;
					}
				}
			}

		}
		return false;
	}

	/**
	 * @param compUnit
	 * @param annotation
	 * @return int
	 * @throws JavaModelException
	 */
	private int getLineNumber(ICompilationUnit compUnit, IAnnotation annotation) throws JavaModelException {
		Document document = new Document(compUnit.getBuffer().getContents());

		try {
			return document.getLineOfOffset(annotation.getSourceRange().getOffset()) + 1;
		} catch (BadLocationException e) {
			return 0;
		}
	}

	private List<CucumberAnnotation> getAllAnnotationsInPackage(final IJavaProject javaProject,
			final String packageFrag, final String lang) throws CoreException, JavaModelException {

		SearchPattern pattern = SearchPattern.createPattern(packageFrag, IJavaSearchConstants.PACKAGE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE);

		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaProject.getPackageFragments());

		final List<CucumberAnnotation> annotations = new ArrayList<CucumberAnnotation>();

		SearchRequestor requestor = new SearchRequestor() {
			@SuppressWarnings("deprecation")
			public void acceptSearchMatch(SearchMatch match) {
				try {
					if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
						IPackageFragment frag = (IPackageFragment) match.getElement();
						for (IClassFile cls : frag.getClassFiles()) {
							IType t = cls.findPrimaryType();
							if (t.isAnnotation()) {
								annotations.add(new CucumberAnnotation(t.getElementName(), lang));
							}
						}
					}
				} catch (JavaModelException e) {
				}
			}
		};
		SearchEngine engine = new SearchEngine();
		jdtSearch(engine, pattern, scope, requestor);
		return annotations;
	}

	private void jdtSearch(SearchEngine engine, SearchPattern pattern, IJavaSearchScope scope,
			SearchRequestor requestor) throws CoreException {
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					requestor, null);
		} catch (Throwable t) {
			t.printStackTrace();
			// if the search engine failed, skip it is a bug into the JDT plugin
		}
	}

	/**
	 * @param importedAnnotations
	 * @param annotation
	 * @return CucumberAnnotation
	 * @throws JavaModelException
	 */
	private CucumberAnnotation getCukeAnnotation(List<CucumberAnnotation> importedAnnotations, IAnnotation annotation)
			throws JavaModelException {

		Matcher m = cukeAnnotationMatcher.matcher(annotation.getElementName());
		if (m.find()) {
			return new CucumberAnnotation(m.group(2), m.group(1));
		}
		for (CucumberAnnotation cuke : importedAnnotations) {
			if (cuke.getAnnotation().equals(annotation.getElementName()))
				return cuke;
		}
		return null;
	}

	/**
	 * @param annotation
	 * @return String
	 * @throws JavaModelException
	 */
	private String getAnnotationText(IAnnotation annotation) throws JavaModelException {
		for (IMemberValuePair mvp : annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}

	
	
	@Override
	protected Set<StepDefinition> findStepDefinitionsFromSupportedResource(IResource stepDefinitionResource,
			MarkerFactory markerFactory, IProgressMonitor monitor) throws CoreException {
//		System.out.println("jdt.findStepDefintions on " + stepDefinitionFile.getName());
		// This IStepDefinitions scans only Java files from Java project
		
		if(stepDefinitionResource instanceof IProject) {
			IProject project = (IProject) stepDefinitionResource;
			boolean isJavaProject = this.support(project);
			if(isJavaProject) {
				IJavaProject javaProject = JavaCore.create(project);
				return findStepDefinitionsInClasspath(javaProject, monitor);
			}
		}
		
		IProject project = stepDefinitionResource.getProject();

		boolean isJavaProject = this.support(project);
		if (!isJavaProject) {
			return new HashSet<StepDefinition>();
		}

		// is a Java compilation unit
		IJavaElement javaElement = JavaCore.create(stepDefinitionResource);
		boolean isCompilationUnit = javaElement instanceof ICompilationUnit;
		if (!isCompilationUnit) {
			return new HashSet<StepDefinition>();
		}

		ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;

		if (CucumberJavaPreferences.isUseStepDefinitionsFilters()) {
			String[] filters = CucumberJavaPreferences.getStepDefinitionsFilters();
			CompilationUnitStepDefinitionsPreferencesFilter filter = new CompilationUnitStepDefinitionsPreferencesFilter(filters);
			if (!filter.accept(compilationUnit)) {
				// skip
				return new HashSet<StepDefinition>();
			}
		}

		List<StepDefinition> stepDefinitions = this.getCukeSteps(compilationUnit, markerFactory, monitor);

		return new HashSet<StepDefinition>(stepDefinitions);
	}

	@Override
	public boolean support(IProject project) throws CoreException {
		return project.isOpen() && project.hasNature(JavaCore.NATURE_ID);
	}

	@Override
	public boolean support(IResource resource) throws CoreException {
		IJavaElement javaElement = JavaCore.create(resource);
		return javaElement != null;
	}


	private Set<StepDefinition> findStepDefinitionsInClasspath(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException {

		SearchPattern searchPattern = SearchPattern.createPattern("cucumber.api.java.*.*", IJavaSearchConstants.TYPE,
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

				public void acceptSearchMatch(SearchMatch match) {
					try {

						if (match.getAccuracy() == SearchMatch.A_ACCURATE) {

							if (match.getElement() instanceof IMethod) {
								IMethod method = (IMethod) match.getElement();
								
								Set<StepDefinition> methodStepDefinitions = getCukeSteps(method);
								if(methodStepDefinitions != null) {
									stepDefinitions.addAll(methodStepDefinitions);
								}

							}
							else if (match.getElement() instanceof IType) {
								IType resolvedType = (IType) match.getElement();
								
								IMethod[] methods = resolvedType.getMethods();
								for (IMethod method : methods) {
									Set<StepDefinition> methodStepDefinitions = getCukeSteps(method);
									if(methodStepDefinitions != null) {
										stepDefinitions.addAll(methodStepDefinitions);
									}
								}
							}
						}

					} catch (CucumberExpressionException e) {
						e.printStackTrace();
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			};

			for (IJavaSearchScope scope : scopes) {
				jdtSearch(engine, searchPattern, scope, requestor);
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

	private IJavaSearchScope[] computeScope(SearchEngine searchEngine, IJavaProject javaProject, IProgressMonitor monitor) throws CoreException {
		// TODO improve monitor support
		List<IJavaSearchScope> scope = new ArrayList<IJavaSearchScope>();
		if (CucumberJavaPreferences.isUseStepDefinitionsFilters()) {
			String[] filters = CucumberJavaPreferences.getStepDefinitionsFilters();
			final List<IJavaElement> pkgScope = new ArrayList<IJavaElement>();
			final List<IJavaElement> typeScope = new ArrayList<IJavaElement>();
			
			IJavaSearchScope projectScope = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject });
			SearchRequestor requestor = new SearchRequestor() {
				@Override
				public void acceptSearchMatch(SearchMatch match) throws CoreException {
					if(match.getAccuracy() == SearchMatch.A_ACCURATE) {
						if(match.getElement() instanceof IPackageFragment) {
							pkgScope.add((IJavaElement) match.getElement());
						}
						else if(match.getElement() instanceof IType) {
							typeScope.add((IJavaElement) match.getElement());
						}
					}
				}
			};
			
			for (String filter : filters) {
				if(filter.endsWith(".*")) {
					// search for a package
					String filterWithoutStar = filter.substring(0, filter.length() - 2);
					SearchPattern searchPattern = SearchPattern.createPattern(filterWithoutStar, IJavaSearchConstants.PACKAGE,
							IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
					
					searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, projectScope,
							requestor, monitor);
				}
				else {
					// search for a type
					SearchPattern searchPattern = SearchPattern.createPattern(filter, IJavaSearchConstants.TYPE,
							IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
					
					searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, projectScope,
							requestor, monitor);
				}
			}
			if(!pkgScope.isEmpty()) {
				scope.add(SearchEngine.createJavaSearchScope(pkgScope.toArray(new IJavaElement[pkgScope.size()]),
						IJavaSearchScope.APPLICATION_LIBRARIES));	
			}
			if(!typeScope.isEmpty()) {
				scope.add(SearchEngine.createJavaSearchScope(typeScope.toArray(new IJavaElement[typeScope.size()]),
						IJavaSearchScope.APPLICATION_LIBRARIES));
			}
			
		}
		else {
			scope.add(SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject },
					IJavaSearchScope.APPLICATION_LIBRARIES));
		}
		return scope.toArray(new IJavaSearchScope[scope.size()]);
	}
	
	private Set<StepDefinition> getCukeSteps(IMethod method) throws JavaModelException {
		Set<StepDefinition> stepDefinitions = new HashSet<StepDefinition>();
		if (CucumberJavaPreferences.isUseStepDefinitionsFilters()) {
			String[] filters = CucumberJavaPreferences.getStepDefinitionsFilters();
			MethodStepDefinitionsPreferencesFilter filter = new MethodStepDefinitionsPreferencesFilter(filters);
			if (!filter.accept(method)) {
				// skip
				return null;
			}
		}
		
		IAnnotation[] annotations = method.getAnnotations();
		for (IAnnotation annotation : annotations) {
			CucumberAnnotation cukeAnnotation = getCukeAnnotation(
					new ArrayList<CucumberAnnotation>(), annotation);
			if (cukeAnnotation != null) {
				StepDefinition stepDefinition = new StepDefinition();
				stepDefinition.setText(getAnnotationText(annotation));
				IClassFile classFile = method.getClassFile();
				IJavaElement pkg = classFile.getParent();
				IJavaElement jar = pkg.getParent();
				String classFileName = classFile.getElementName();
				String packageName = pkg.getElementName();
				String jarName = jar.getElementName();
				stepDefinition.setSourceName(classFileName);
				stepDefinition.setPackageName(packageName);
				stepDefinition.setJDTHandleIdentifier(method.getHandleIdentifier());
				stepDefinition.setLabel(String.format("%s > %s.%s#%s%s", jarName, packageName, classFileName, method.getElementName(), method.getSignature()));
				stepDefinition.setLang(cukeAnnotation.getLang());
				stepDefinitions.add(stepDefinition);
			}
		}
		return stepDefinitions;
	}
}
