package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepPreferences;
import cucumber.eclipse.steps.integration.StepsChangedEvent;

/*
 * Modified for Issue #211 : Duplicate Step definitions
 * Inheriting this class to cucumber.eclipse.editor.steps.jdt.JDTStepDefinitions child class
 * 
 */

//Commented for Issue #211 : Duplicate Step definitions
//public class StepDefinitions implements IStepDefinitions {

public class StepDefinitions extends MethodDefinition implements IStepDefinitions {

	protected static StepDefinitions INSTANCE = new StepDefinitions();

	private final Pattern cukeAnnotationMatcher = Pattern.compile("cucumber\\.api\\.java\\.([a-z_]+)\\.(.*)$");
	private static final String CUCUMBER_API_JAVA = "cucumber.api.java.";
	private static final String CUCUMBER_API_JAVA8 = "cucumber.api.java8.";

	public String JAVA_PROJECT = "org.eclipse.jdt.core.javanature";
	public int JAVA_SOURCE = IPackageFragmentRoot.K_SOURCE;
	public int JAVA_JAR_BINARY = IPackageFragmentRoot.K_BINARY;

	public String COMMA = ",";
	
	//public List<IStepListener> listeners = new ArrayList<IStepListener>();
	
	//#240:For Changes in step implementation is reflected in feature file
	private List<IStepListener> listeners = new ArrayList<IStepListener>();

	// secure usage of the singleton
	private StepDefinitions() {
	}

	/**
	 * Initialize
	 * 
	 * @return StepDefinitions
	 */
	protected static StepDefinitions getInstance() {
		return INSTANCE;
	}

	@Override
	public String supportedLanguage() {
		return "java";
	}
	
	/*
	 * Commented due to Issue #211 : Duplicate Step definitions Redefined in
	 * cucumber.eclipse.editor.steps.jdt.JDTStepDefinitions child class
	 */
	// 1. To get Steps as Set from java file
	/*
	 * @Override public Set<Step> getSteps(IFile featurefile) {
	 * 
	 * Set<Step> steps = new LinkedHashSet<Step>();
	 * 
	 * IProject project = featurefile.getProject(); try { if
	 * (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
	 * IJavaProject javaProject = JavaCore.create(project);
	 * 
	 * //Issue #211 : Duplicate Step definitions List<IJavaProject>
	 * projectsToScan = new ArrayList<IJavaProject>();
	 * projectsToScan.add(javaProject);
	 * projectsToScan.addAll(getRequiredJavaProjects(javaProject));
	 * 
	 * for (IJavaProject currentJavaProject: projectsToScan) {
	 * scanJavaProjectForStepDefinitions(currentJavaProject, steps); } } } catch
	 * (CoreException e) { e.printStackTrace(); }
	 * 
	 * return steps; }
	 */

	// From Java-Source-File(.java) : Collect All Steps as List based on
	// Cucumber-Annotations
	public List<Step> getCukeSteps(ICompilationUnit iCompUnit, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {
		
		long start = System.currentTimeMillis();

		List<Step> steps = new ArrayList<Step>();
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
				setJava8CukeLang(importDeclaration);
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
							String lang = ifType.getElementName().toLowerCase();
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
										definition.setJava8CukeLang(lang);
									}
								}
							}
							//Iterate MethodDefinition
							for (MethodDefinition method : methodDefList) {
								//Iterate Method-Statements
								for (Statement statement : method.getMethodBodyList()) {					
									// Add all lambda-steps to Step
									Step step = new Step();
									step.setSource(iCompUnit.getResource());	//source
									try {
										String lambdaStep = method.getLambdaStep(statement, keyWords);
										if (lambdaStep == null) {
											continue;
										}
										step.setText(lambdaStep);	//step
										step.setLineNumber(javaParser.getLineNumber(statement));	//line-number
										step.setLang(method.getCukeLang());	//Language
										steps.add(step);
									} catch(PatternSyntaxException e) {
										//we can't parse the pattern so we can't use the step!
										System.out.println(e);
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
						Step step = new Step();
						step.setSource(method.getResource());
						step.setText(getAnnotationText(annotation));
						step.setLineNumber(getLineNumber(iCompUnit, annotation));
						step.setLang(cukeAnnotation.getLang());
						steps.add(step);
					}
				}

			}
		}
		
		long end = System.currentTimeMillis();
		System.out.println("getCukeSteps " + iCompUnit.getJavaProject().getElementName() + ": " + iCompUnit.getElementName() + " " + (end - start) + " ms.");
		
		return steps;
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
	public List<Step> getCukeSteps(IPackageFragment javaPackage, IClassFile classFile)
			throws JavaModelException, CoreException {

		List<Step> steps = new ArrayList<Step>();
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
							Step step = new Step();
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
	public void scanJavaProjectForStepDefinitions(IJavaProject projectToScan, Collection<Step> collectedSteps, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {

		IPackageFragment[] packages = projectToScan.getPackageFragments();

		for (IPackageFragment javaPackage : packages) {

			if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

				for (ICompilationUnit compUnit : javaPackage.getCompilationUnits()) {
					collectedSteps.addAll(getCukeSteps(compUnit, progressMonitor));
				}
			}
		}
	}

	/*
	 * Commented due to Issue #211 : Duplicate Step definitions Redefined in
	 * 'cucumber.eclipse.editor.steps.jdt.JDTStepDefinitions' child class
	 */
	/*
	 * @Override public void addStepListener(IStepListener listener) {
	 * this.listeners.add(listener); }
	 */

	
	
	
	public void notifyListeners(StepsChangedEvent event) {
		for (IStepListener listener : listeners) {
			listener.onStepsChanged(event);
		}
	}
	
	@Override
	public void scan(IFile stepDefinitionFile) {
		long start = System.currentTimeMillis();
		this.notifyListeners(new StepsChangedEvent(stepDefinitionFile));
		long end = System.currentTimeMillis();
		System.out.println("StepDefinitions for Java " + (stepDefinitionFile == null ? "" : stepDefinitionFile.getName() + " ") + "scanned in " + (end - start) + " ms.");
	}
	
	@Override
	public void scan() {
		this.scan(null);
	}

	private StepPreferences stepPreferences = StepPreferences.INSTANCE;
	
	private void scanProject(IProject project, IFile featurefile, Set<Step> steps, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
		try {
			// Get Package name/s from 'User-Settings' preference
			final String externalPackageName = this.stepPreferences.getPackageName();
			//System.out.println("Package Names = " + externalPackageName);
			String[] extPackages = externalPackageName.trim().split(COMMA);
			
			//#239:Only match step implementation in same package as feature file
			final boolean onlyPackages = this.stepPreferences.getOnlyPackages();
			final String onlySpeficicPackagesValue = this.stepPreferences.getOnlySpecificPackage().trim();
			final boolean onlySpeficicPackages= onlySpeficicPackagesValue.length() == 0 ? false : true;
			String featurefilePackage = featurefile.getParent().getFullPath().toString();
	
			if (project.isNatureEnabled(JAVA_PROJECT)) {
				
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
				SubMonitor subMonitor = SubMonitor.convert(progressMonitor, packages.length);
				for (IPackageFragment javaPackage : packages) {
					// Get Packages from source folder of current project
					// #239:Only match step implementation in same package as feature file
					if (javaPackage.getKind() == JAVA_SOURCE ) {
						subMonitor.subTask("Scanning "+javaPackage.getPath().toString());
						if 	((!onlyPackages || featurefilePackage.startsWith(javaPackage.getPath().toString())) && 
							(!onlySpeficicPackages || javaPackage.getElementName().startsWith(onlySpeficicPackagesValue))) {
							
							// System.out.println("Package Name-1
							// :"+javaPackage.getElementName());
							// Collect All Steps From Source
							collectCukeStepsFromSource(javaPackage, steps, progressMonitor);
						}
					}
	
					// Get Packages from JAR exists in class-path
					if ((javaPackage.getKind() == JAVA_JAR_BINARY) && !externalPackageName.equals("")) {
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
	private void collectCukeStepsFromSource(IPackageFragment javaPackage, Set<Step> steps, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {

		long start = System.currentTimeMillis();
		for (ICompilationUnit iCompUnit : javaPackage.getCompilationUnits()) {
			// Collect and add Steps
			steps.addAll(getCukeSteps(iCompUnit, progressMonitor));
		}
		long end = System.currentTimeMillis();
		System.out.println("collectCukeStepsFromSource " + javaPackage.getJavaProject().getElementName() + ": " + javaPackage.getElementName() + " " + (end - start) + " ms.");
	}
	
	/**
	 * Collect all cuke-steps from .class file of Jar
	 * 
	 * @param javaPackage
	 * @param steps
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private void collectCukeStepsFromJar(IPackageFragment javaPackage, Set<Step> steps)
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
	public void addStepListener(IStepListener listener) {
		this.listeners.add(listener);	
	}
	
	@Override
	public void removeStepListener(IStepListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public Set<Step> getSteps(IFile featureFile, IProgressMonitor progressMonitor) throws CoreException {

		IProject project = featureFile.getProject();

		
		Set<Step> steps = new LinkedHashSet<Step>();
		
		try {
			//Scan project and direct referenced projects...
			Set<IProject> projects = new LinkedHashSet<IProject>();
			if(project.isAccessible()) { // skip closed project
				projects.add(project);
				projects.addAll(Arrays.asList(project.getReferencedProjects()));
				SubMonitor subMonitor = SubMonitor.convert(progressMonitor, projects.size());
				for (IProject projectToScan : projects) {
					scanProject(projectToScan, featureFile, steps, subMonitor.newChild(1));
				}
			}
		} finally {
			if (progressMonitor != null) {
				 progressMonitor.done();
	         }
		}
		return steps;
	}
	
	@Override
	public boolean support(IProject project) {
		IJavaProject javaProject= JavaCore.create(project);
        if (javaProject == null) {
            // the project is not configured for Java (has no Java nature)
            return false;
        }
	        
		return true;
	}
	

}