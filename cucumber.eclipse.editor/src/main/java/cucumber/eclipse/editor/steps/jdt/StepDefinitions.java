package cucumber.eclipse.editor.steps.jdt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import cucumber.eclipse.editor.preferences.CucumberUserSettingsPage;
import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;

/**
 * @author girija.panda@nokia.com
 * 
 * Purpose: Redefining 'cucumber.eclipse.steps.jdt.StepDefinitions' class in order to avoid plugin-dependency conflicts.
 * 			Supports reusing of Step-Definitions from external JAR(.class) exists in class-path.
 * 			Reads the package name of external JAR from 'User Settings' of Cucumber-Preference page
 * 			and Populate the step proposals from external JAR 
 */
public class StepDefinitions implements IStepDefinitions {

	private Pattern cukeAnnotationMatcher = Pattern.compile("cucumber\\.api\\.java\\.([a-z_]+)\\.(.*)$");

	// Newly Declared By Girija
	// To Collect all Steps as Set for ContentAssistance
	public static Set<Step> steps = null;

	// Read package name from 'User-Settings' prefrence
	private String externalPackageName = null;
	private CucumberUserSettingsPage userSettingsPage = null;
	
	// 1. To get Steps as Set from java file
	@Override
	public Set<Step> getSteps(IFile featurefile) {

		// System.out.println("StepDefinitions : getSteps()....In");

		// Commented By Girija to use LinkedHashSet
		// Set<Step> steps = new HashSet<Step>();

		// Instead of above HashSet
		// Used LinkedHashSet : Import all steps from step-definition File
		steps = new LinkedHashSet<Step>();

		IProject project = featurefile.getProject();
		
		try 
		{
		
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {

				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
				
				//Get Package name
				this.externalPackageName = readExternalPackage();
				//System.out.println("My External Package Name =" +externalPackageName);
				
				for (IPackageFragment javaPackage : packages) 
				{
					// Get Packages from source folder of current project
					if ((javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE)) {
						
						//System.out.println("javaPackageKind-1 : "+ javaPackage.getKind());
						//System.out.println("Package Name-1 : "+ javaPackage.getElementName());

						for (ICompilationUnit compUnit : javaPackage.getCompilationUnits()) {
							//System.out.println("CompilationUnit-1 : "+ compUnit.getElementName());
							steps.addAll(getCukeAnnotations(javaProject,compUnit));
						}
					}

					
					/*// CucumberUserSettingsPage object
					CucumberUserSettingsPage page = new CucumberUserSettingsPage();					
					String myPackageName = page.getPackageName();					
					System.out.println("My External Package Name = " + myPackageName);*/
					
					// Get Packages from JAR exists in class-path  
					if ((javaPackage.getKind() == IPackageFragmentRoot.K_BINARY) && !this.externalPackageName.equals(""))
					{
						// Check package from external JAR/class file
					   if(javaPackage.getElementName().equals(this.externalPackageName)
							|| javaPackage.getElementName().startsWith(this.externalPackageName))
					   {					
						   //System.out.println("Package Name-2 : "+ javaPackage.getElementName());
						   
						   IClassFile[] classFiles = javaPackage.getClassFiles();	
						   for (IClassFile classFile : classFiles) 
						   {
							   //System.out.println("----classFile: " + classFile.getElementName());						
							   steps.addAll(getCukeAnnotations(javaPackage, classFile));
						   }						
					   }
					}
				}
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}

		//System.out.println("STEPS = " + steps);
		return steps;
	}

	// 2. Get Cucumber-Annotation-Step as List from JAR(.class) file
	private List<Step> getCukeAnnotations(IPackageFragment javaPackage, IClassFile classFile) throws JavaModelException, CoreException {

			List<Step> steps = new ArrayList<Step>();
			List<CucumberAnnotation> importedAnnotations = new ArrayList<CucumberAnnotation>();
		
			//Get content as children
			for (IJavaElement javaElement : classFile.getChildren()) {

				if (javaElement instanceof IType) {
					
					//System.out.println("--------IType "+ javaElement.getElementName());
					
					/*// IInitializer
					IInitializer[] inits = ((IType) javaElement).getInitializers();
					for (IInitializer init : inits) {
						System.out.println("----------------IInitializer: "+ init.getElementName());
					}

					// IField
					IField[] fields = ((IType) javaElement).getFields();
					for (IField field : fields) {
						System.out.println("----------------field: "+ field.getElementName());
					}*/

					// IMethod
					IMethod[] methods = ((IType) javaElement).getMethods();
					for (IMethod method : methods) 
					{
						//System.out.println("----------------method-name: "+ method.getElementName());
						//System.out.println("----------------method return type - "+ method.getReturnType());
						//System.out.println("----------------method-source: "+ classFile.getElementName());
						
						for (IAnnotation annotation : method.getAnnotations()) 
						{
							//System.out.println("Annotation:" + annotation);
							CucumberAnnotation cukeAnnotation = getCukeAnnotation(importedAnnotations, annotation);
							if (cukeAnnotation != null) 
							{
								Step step = new Step();									
									step.setText(getAnnotationText(annotation));
									step.setSourceName(classFile.getElementName());
									step.setPackageName(javaPackage.getElementName());
									// step.setLineNumber(getLineNumber(compUnit,annotation));
									step.setLang(cukeAnnotation.getLang());
									steps.add(step);
								
								//System.out.println("IF-STEPS: " + steps);
							}
						}
					}
				}
			}
		
			//System.out.println("ALL-STEPS:" + steps);
			
		return steps;
	}

	
	
	
	// 2. Get Cucumber-Annotation-Step as List from Source(.java) file
	private List<Step> getCukeAnnotations(IJavaProject javaProject, ICompilationUnit compUnit) throws JavaModelException, CoreException {

		List<Step> steps = new ArrayList<Step>();
		List<CucumberAnnotation> importedAnnotations = new ArrayList<CucumberAnnotation>();

		for (IImportDeclaration decl : compUnit.getImports()) {
			Matcher m = cukeAnnotationMatcher.matcher(decl.getElementName());
			if (m.find()) {
				if ("*".equals(m.group(2))) {
					importedAnnotations.addAll(getAllAnnotationsInPackage(javaProject, "cucumber.api.java." +m.group(1),m.group(1)));
				} else {
					importedAnnotations.add(new CucumberAnnotation(m.group(2), m.group(1)));
				}
			}
		}

		for (IType t : compUnit.getTypes()) {
			for (IMethod method : t.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {
					CucumberAnnotation cukeAnnotation = getCukeAnnotation(importedAnnotations, annotation);
					if (cukeAnnotation != null) {
						Step step = new Step();
							 step.setSource(method.getResource());
							 step.setText(getAnnotationText(annotation));
							 step.setLineNumber(getLineNumber(compUnit, annotation));
							 step.setLang(cukeAnnotation.getLang());
						steps.add(step);
					}
				}
			}
		}
		return steps;
	}

	private int getLineNumber(ICompilationUnit compUnit, IAnnotation annotation) throws JavaModelException {
		
		Document document = new Document(compUnit.getBuffer().getContents());

		try {
			return document.getLineOfOffset(annotation.getSourceRange().getOffset()) + 1;
		} catch (BadLocationException e) {
			return 0;
		}
	}

	
	private List<CucumberAnnotation> getAllAnnotationsInPackage(final IJavaProject javaProject, final String packageFrag, final String lang) throws CoreException, JavaModelException {

		SearchPattern pattern = SearchPattern.createPattern(packageFrag,IJavaSearchConstants.PACKAGE,IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaProject.getPackageFragments());

		final List<CucumberAnnotation> annotations = new ArrayList<CucumberAnnotation>();

		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				try {
					if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
						IPackageFragment frag = (IPackageFragment) match
								.getElement();
						for (IClassFile cls : frag.getClassFiles()) {
							IType t = cls.findPrimaryType();
							if (t.isAnnotation()) {
								annotations.add(new CucumberAnnotation(t
										.getElementName(), lang));
							}
						}
					}
				} catch (JavaModelException e) {
				}
			}
		};

		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);

		return annotations;
	}

	
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

	
	private String getAnnotationText(IAnnotation annotation)throws JavaModelException {
		for (IMemberValuePair mvp : annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}
	
	
	
	// To Read package name from 'User-Settings' preference page
	private String readExternalPackage(){
		this.userSettingsPage = new CucumberUserSettingsPage();					
		String myPackageName = this.userSettingsPage.getPackageName();					
		//System.out.println("My External Package Name =" +myPackageName);
		return myPackageName;
	}

	@Override
	public void addStepListener(IStepListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeStepListener(IStepListener listener) {
		// TODO Auto-generated method stub
		
	}

}
