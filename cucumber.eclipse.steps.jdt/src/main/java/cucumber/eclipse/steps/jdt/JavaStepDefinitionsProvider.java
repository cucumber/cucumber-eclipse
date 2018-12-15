package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
import cucumber.eclipse.steps.integration.StepPreferences;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;
import io.cucumber.cucumberexpressions.CucumberExpressionException;


/** Find step definitions on Java elements.
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


	private String JAVA_PROJECT = "org.eclipse.jdt.core.javanature";
	private int JAVA_SOURCE = IPackageFragmentRoot.K_SOURCE;
	private int JAVA_JAR_BINARY = IPackageFragmentRoot.K_BINARY;
//	private String lang;
	private String COMMA = ",";
	
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


//	public void setJava8CukeLang(String importDeclaration) {
//		this.lang = importDeclaration.substring(importDeclaration.lastIndexOf(".") + 1).toLowerCase();
//	}
	
	// From Java-Source-File(.java) : Collect All Steps as List based on
	// Cucumber-Annotations
	protected List<StepDefinition> getCukeSteps(ICompilationUnit iCompUnit, MarkerFactory markerFactory, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {
		
		long start = System.currentTimeMillis();

		List<StepDefinition> steps = new ArrayList<StepDefinition>();
		List<CucumberAnnotation> importedAnnotations = new ArrayList<CucumberAnnotation>();
		IImportDeclaration[] allimports = iCompUnit.getImports();
		
		for (IImportDeclaration decl : allimports) {

			// Match Package name
			Matcher m = cukeAnnotationMatcher.matcher(decl.getElementName());
			if (m.find()) {
				if ("*".equals(m.group(2))) {
					importedAnnotations.addAll(
							getAllAnnotationsInPackage(iCompUnit.getJavaProject(), CUCUMBER_API_JAVA + m.group(1), m.group(1)));
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
			//collect all steps from java8 lamdas
			for (IType ifType : t.newTypeHierarchy(progressMonitor).getAllInterfaces()) {
				
				if (ifType.isInterface() && ifType.getFullyQualifiedName().startsWith(CUCUMBER_API_JAVA8)) {
					String[] superInterfaceNames = ifType.getSuperInterfaceNames();
					for (String superIfName : superInterfaceNames) {
						if (superIfName.endsWith(".LambdaGlueBase")) {
							//we found a possible interface, now try to load the language...
//							String lang = ifType.getElementName().toLowerCase();
							//init if not done in previous step..
							if (javaParser == null)  {
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
										MethodDefinition definition = new MethodDefinition(method.getName(), method.getReturnType2(), statementList);
										methodDefList.add(definition);
//										definition.setJava8CukeLang(lang);
									}
								}
							}
							//Iterate MethodDefinition
							for (MethodDefinition method : methodDefList) {
								//Iterate Method-Statements
								for (Statement statement : method.getMethodBodyList()) {					
									// Add all lambda-steps to Step
									StepDefinition step = new StepDefinition();
									step.setSource(iCompUnit.getResource());	//source
									String lambdaStep = method.getLambdaStep(statement, keyWords);
									if (lambdaStep == null) {
										continue;
									}
									int lineNumber = javaParser.getLineNumber(statement);
									try {
										step.setText(lambdaStep);	//step
										step.setLineNumber(lineNumber);	//line-number
										step.setLang(method.getCukeLang());	//Language
										steps.add(step);
									} catch(CucumberExpressionException e) {
										markerFactory.syntaxErrorOnStepDefinition(iCompUnit.getResource(), e, lineNumber);
									}
									
								}
							}
						}
					}
				}
			}
			// Collect all steps from Annotations used in the methods as per imported Annotations
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
						} catch(CucumberExpressionException e) {
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
	public boolean isCukeLambdaExpr(MethodDeclaration method, Set<String> keywords) {
		@SuppressWarnings("unchecked")
		List<Statement> statements = method.getBody().statements();
		for (Statement statement : statements) {
			if (statement instanceof ExpressionStatement) {
				ExpressionStatement expressionStatement = (ExpressionStatement) statement;
				Expression expression = expressionStatement.getExpression();
				if (expression instanceof MethodInvocation) {
					String identifier = ((MethodInvocation) expression).getName().getIdentifier();
					if (keywords.contains(identifier)) {
						//we found a lamda
						return true;
					}
				}
			}
			
		}
		return false;
	}
	
	/**
	 * From JAR-File(.class) : Collect All Steps as List based on
	 * Cucumber-Annotations
	 * 
	 * @param javaPackage
	 * @param classFile
	 * @return List<Step>
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public List<StepDefinition> getCukeSteps(IPackageFragment javaPackage, IClassFile classFile)
			throws JavaModelException, CoreException {

		List<StepDefinition> steps = new ArrayList<StepDefinition>();
		List<CucumberAnnotation> importedAnnotations = new ArrayList<CucumberAnnotation>();

		// Get content as children
		for (IJavaElement javaElement : classFile.getChildren()) {

			if (javaElement instanceof IType) {

				// System.out.println("--------IType "
				// +javaElement.getElementName());
				/*
				 * IInitializer IInitializer[] inits = ((IType)
				 * javaElement).getInitializers(); for (IInitializer init :
				 * inits) { System.out.println("----------------IInitializer: "+
				 * init.getElementName()); } IField IField[] fields =
				 * ((IType)javaElement).getFields(); for (IField field : fields)
				 * { System.out.println("----------------field: "
				 * +field.getElementName()); }
				 */

				// IMethod
				IMethod[] methods = ((IType) javaElement).getMethods();
				for (IMethod method : methods) {
					// System.out.println("----------------method-name :
					// "+method.getElementName());
					// System.out.println("----------------method return type :
					// "+method.getReturnType());
					// System.out.println("----------------method-source :
					// +classFile.getElementName());

					for (IAnnotation annotation : method.getAnnotations()) {
						CucumberAnnotation cukeAnnotation = getCukeAnnotation(importedAnnotations, annotation);
						if (cukeAnnotation != null) {
							StepDefinition step = new StepDefinition();
							step.setText(getAnnotationText(annotation));
							step.setSourceName(classFile.getElementName());
							step.setPackageName(javaPackage.getElementName());
							// step.setLineNumber(getLineNumber(compUnit,annotation));
							step.setLang(cukeAnnotation.getLang());
							steps.add(step);
						}
					}
				}
			}
		}

		return steps;
	}

	/**
	 * @param compUnit
	 * @param annotation
	 * @return int
	 * @throws JavaModelException
	 */
	public int getLineNumber(ICompilationUnit compUnit, IAnnotation annotation) throws JavaModelException {
		Document document = new Document(compUnit.getBuffer().getContents());

		try {
			return document.getLineOfOffset(annotation.getSourceRange().getOffset()) + 1;
		} catch (BadLocationException e) {
			return 0;
		}
	}

	/**
	 * @param javaProject
	 * @param packageFrag
	 * @param lang
	 * @return List<CucumberAnnotation>
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	public List<CucumberAnnotation> getAllAnnotationsInPackage(final IJavaProject javaProject, final String packageFrag,
			final String lang) throws CoreException, JavaModelException {

		SearchPattern pattern = SearchPattern.createPattern(packageFrag, IJavaSearchConstants.PACKAGE,
				IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);

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
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor,
				null);

		return annotations;
	}

	/**
	 * @param importedAnnotations
	 * @param annotation
	 * @return CucumberAnnotation
	 * @throws JavaModelException
	 */
	public CucumberAnnotation getCukeAnnotation(List<CucumberAnnotation> importedAnnotations, IAnnotation annotation)
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
	public String getAnnotationText(IAnnotation annotation) throws JavaModelException {
		for (IMemberValuePair mvp : annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}

	/**
	 * @param javaProject
	 * @return List<IJavaProject>
	 * @throws CoreException
	 */
	public static List<IJavaProject> getRequiredJavaProjects(IJavaProject javaProject) throws CoreException {

		List<String> requiredProjectNames = Arrays.asList(javaProject.getRequiredProjectNames());

		List<IJavaProject> requiredProjects = new ArrayList<IJavaProject>();

		for (String requiredProjectName : requiredProjectNames) {

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(requiredProjectName);

			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {

				requiredProjects.add(JavaCore.create(project));
			}
		}
		return requiredProjects;
	}

	/**
	 * @param projectToScan
	 * @param collectedSteps
	 * @param progressMonitor 
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public void scanJavaProjectForStepDefinitions(IJavaProject projectToScan, Collection<StepDefinition> collectedSteps, MarkerFactory markerFactory, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {

		IPackageFragment[] packages = projectToScan.getPackageFragments();

		for (IPackageFragment javaPackage : packages) {

			if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

				for (ICompilationUnit compUnit : javaPackage.getCompilationUnits()) {
					collectedSteps.addAll(getCukeSteps(compUnit, markerFactory, progressMonitor));
				}
			}
		}
	}

	private StepPreferences stepPreferences = StepPreferences.INSTANCE;
	
	private void scanProject(IProject project, IFile featurefile, Set<StepDefinition> steps, MarkerFactory markerFactory, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
		try {
			System.out.println("Scanning project " + project.getName());
			
			// Get Package name/s from 'User-Settings' preference
			final String externalPackageName = this.stepPreferences.getPackageName();
			//System.out.println("Package Names = " + externalPackageName);
			String[] extPackages = externalPackageName.trim().split(COMMA);
			
			//#239:Only match step implementation in same package as feature file
			final boolean onlyPackages = this.stepPreferences.getOnlyPackages();
			final String onlySpeficicPackagesValue = this.stepPreferences.getOnlySpecificPackage().trim();
			final boolean onlySpeficicPackages= onlySpeficicPackagesValue.length() == 0 ? false : true;
//			String featurefilePackage = featurefile.getParent().getFullPath().toString();
			String featurefilePackage = "";
			if(featurefile != null) {
				featurefilePackage = featurefile.getParent().getFullPath().toString();
			}
	
			if (project.isNatureEnabled(JAVA_PROJECT)) {
				
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
				SubMonitor subMonitor = SubMonitor.convert(progressMonitor, packages.length);
				for (IPackageFragment javaPackage : packages) {
					String packageName = javaPackage.getElementName();
					if(packageName == null || "".equals(packageName)) {
						packageName = "default";
					}
					// Get Packages from source folder of current project
					// #239:Only match step implementation in same package as feature file
					if (javaPackage.getKind() == JAVA_SOURCE ) {
//						System.out.println("Scanning package " + packageName);
						subMonitor.subTask("Scanning "+javaPackage.getPath().toString());
						if 	((!onlyPackages || featurefilePackage.startsWith(javaPackage.getPath().toString())) && 
							(!onlySpeficicPackages || javaPackage.getElementName().startsWith(onlySpeficicPackagesValue))) {
							
							// System.out.println("Package Name-1
							// :"+javaPackage.getElementName());
							// Collect All Steps From Source
							collectCukeStepsFromSource(javaPackage, steps, markerFactory, progressMonitor);
						}
					}
	
					// Get Packages from JAR exists in class-path
					else if ((javaPackage.getKind() == JAVA_JAR_BINARY) && !externalPackageName.equals("")) {
						System.out.println("Scanning jar package " + packageName);
						subMonitor.subTask("Scanning package "+javaPackage.getElementName());
						// Iterate all external packages
						for (String extPackageName : extPackages) {
							// Check package from external JAR/class file
							if (javaPackage.getElementName().equals(extPackageName.trim())
									|| javaPackage.getElementName().startsWith(extPackageName.trim())) {
								// Collect All Steps From JAR
								collectCukeStepsFromJar(javaPackage, steps);
							}
						}
					}
					else {
//						System.out.println("Skip package " + packageName);
					}
					subMonitor.worked(1);
				}
			}
		} finally {
			if (progressMonitor != null) {
				 progressMonitor.done();
	         }
		}
	}
	
	/**
	 * Collect all cuke-steps from java-source Files
	 * 
	 * @param javaProject
	 * @param javaPackage
	 * @param steps
	 * @param progressMonitor 
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private void collectCukeStepsFromSource(IPackageFragment javaPackage, Set<StepDefinition> steps, MarkerFactory markerFactory, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {

		long start = System.currentTimeMillis();
		ICompilationUnit[] compilationUnits = javaPackage.getCompilationUnits();
		for (ICompilationUnit iCompUnit : compilationUnits) {
			// Collect and add Steps
			steps.addAll(getCukeSteps(iCompUnit, markerFactory, progressMonitor));
		}
		long end = System.currentTimeMillis();
//		System.out.println("collectCukeStepsFromSource " + javaPackage.getJavaProject().getElementName() + ": " + javaPackage.getElementName() + " " + (end - start) + " ms.");
	}
	
	/**
	 * Collect all cuke-steps from .class file of Jar
	 * 
	 * @param javaPackage
	 * @param steps
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private void collectCukeStepsFromJar(IPackageFragment javaPackage, Set<StepDefinition> steps)
			throws JavaModelException, CoreException {
		
		long start = System.currentTimeMillis();
		@SuppressWarnings("deprecation")
		IClassFile[] classFiles = javaPackage.getClassFiles();
		for (IClassFile classFile : classFiles) {
			// System.out.println("----classFile: "
			// +classFile.getElementName());
			steps.addAll(getCukeSteps(javaPackage, classFile));
		}
		long end = System.currentTimeMillis();

		System.out.println("collectCukeStepsFromJar " + javaPackage.getJavaProject().getElementName() + ": " + javaPackage.getElementName() + " " + (end - start) + " ms.");

	}
	
	@Override
	protected Set<StepDefinition> findStepDefinitionsFromSupportedResource(IFile stepDefinitionFile,
			MarkerFactory markerFactory, IProgressMonitor monitor) throws CoreException {
//		System.out.println("jdt.findStepDefintions on " + stepDefinitionFile.getName());
		// This IStepDefinitions scans only Java files from Java project
		IProject project = stepDefinitionFile.getProject();
		
		boolean isJavaProject = this.support(project);
		if(!isJavaProject) {
			return new HashSet<StepDefinition>();
		}

		// is a Java compilation unit
		IJavaElement javaElement = JavaCore.create(stepDefinitionFile);
		boolean isCompilationUnit = javaElement instanceof ICompilationUnit;
		if(!isCompilationUnit) {
			return new HashSet<StepDefinition>();
		}
		
		ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
		List<StepDefinition> stepDefinitions = this.getCukeSteps(compilationUnit, markerFactory, monitor);
		
		return new HashSet<StepDefinition>(stepDefinitions);
	}
	
	@Override
	public boolean support(IProject project) throws CoreException {
        return project.isOpen() && project.hasNature(JavaCore.NATURE_ID);
	}
	
}