package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepsChangedEvent;

/*
 * Modified for Issue #211 : Duplicate Step definitions
 * Inheriting this class in cucumber.eclipse.editor.steps.jdt.JDTStepDefinitions child class
 * 
 */

//Commented for Issue #211 : Duplicate Step definitions
//public class StepDefinitions implements IStepDefinitions {

public class StepDefinitions {
	
	public static volatile StepDefinitions INSTANCE;
	
	public List<IStepListener> listeners = new ArrayList<IStepListener>();

	public final Pattern cukeAnnotationMatcher = Pattern.compile("cucumber\\.api\\.java\\.([a-z_]+)\\.(.*)$");
	
	public StepDefinitions() {
	
	}

	public static StepDefinitions getInstance() {
		if (INSTANCE == null) {
			synchronized (StepDefinitions.class) {
				if (INSTANCE == null) {
					INSTANCE = new StepDefinitions();
				}
			}
		}
		
		return INSTANCE;
	}
	
	
	
	/*Commented due to Issue #211 : Duplicate Step definitions
	 * Redefined in cucumber.eclipse.editor.steps.jdt.JDTStepDefinitions child class
	 */
	//1. To get Steps as Set from java file
	/*@Override
	public Set<Step> getSteps(IFile featurefile) {
		
		System.out.println("Load StepDefinitions:getSteps()....In package : cucumber.eclipse.steps.jdt");

		
		Set<Step> steps = new LinkedHashSet<Step>();
		
		IProject project = featurefile.getProject();	
		try 
		{
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) 
			{				
				IJavaProject javaProject = JavaCore.create(project);
				
				//Issue #211 : Duplicate Step definitions
				List<IJavaProject> projectsToScan = new ArrayList<IJavaProject>();				
				projectsToScan.add(javaProject);				
				projectsToScan.addAll(getRequiredJavaProjects(javaProject));
				
				for (IJavaProject currentJavaProject: projectsToScan) {				
					scanJavaProjectForStepDefinitions(currentJavaProject, steps);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} 
		
		return steps;
	}*/
	
	

	// 2. Get Step as List of Cucumber-Annotation from java file
	public List<Step> getCukeAnnotations(IJavaProject javaProject, ICompilationUnit compUnit)
			throws JavaModelException, CoreException {
		
		List<Step> steps = new ArrayList<Step>();	
		List<CucumberAnnotation> importedAnnotations = new ArrayList<CucumberAnnotation>();
		
		for (IImportDeclaration decl : compUnit.getImports()) 
		{
			Matcher m = cukeAnnotationMatcher.matcher(decl.getElementName());
			if (m.find()) {
				if ("*".equals(m.group(2))) 
				{
					importedAnnotations.addAll(getAllAnnotationsInPackage(javaProject, "cucumber.api.java."+m.group(1), m.group(1)));
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
	
	
	public int getLineNumber(ICompilationUnit compUnit, IAnnotation annotation) throws JavaModelException {
		Document document = new Document(compUnit.getBuffer().getContents()); 
		
		try {
			return document.getLineOfOffset(annotation.getSourceRange().getOffset()) + 1;
		} catch (BadLocationException e) {
			return 0;
		}
	}
	
	
	public  List<CucumberAnnotation> getAllAnnotationsInPackage(final IJavaProject javaProject, final String packageFrag, final String lang) throws CoreException, JavaModelException {

		SearchPattern pattern = SearchPattern.createPattern(packageFrag, IJavaSearchConstants.PACKAGE , IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaProject.getPackageFragments());
		
		final List<CucumberAnnotation> annotations = new ArrayList<CucumberAnnotation>();
		
		SearchRequestor requestor = new SearchRequestor() {
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
				} catch (JavaModelException e) {}
			}
		};
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() }, scope, requestor,
				null);
		
		return annotations;
	}
	
	
	public CucumberAnnotation getCukeAnnotation(List<CucumberAnnotation> importedAnnotations,
			IAnnotation annotation) throws JavaModelException {
		
		Matcher m = cukeAnnotationMatcher.matcher(annotation.getElementName());
		if (m.find()) {
			return new CucumberAnnotation(m.group(2), m.group(1));
		}
		for (CucumberAnnotation cuke : importedAnnotations) {
			if (cuke.getAnnotation().equals(annotation.getElementName())) return cuke;
		}
		return null; 
	}

	
	public String getAnnotationText(IAnnotation annotation) throws JavaModelException {
		for (IMemberValuePair mvp :  annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}
	
	
	
	
	
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

	public void scanJavaProjectForStepDefinitions(IJavaProject projectToScan, Collection<Step> collectedSteps)
			throws JavaModelException, CoreException {
		
		IPackageFragment[] packages = projectToScan.getPackageFragments();
		
		for (IPackageFragment javaPackage : packages) {

			if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

				for (ICompilationUnit compUnit : javaPackage.getCompilationUnits()) {
					collectedSteps.addAll(getCukeAnnotations(projectToScan, compUnit));
				}
			}
		}
	}
	
	
	/*Commented due to Issue #211 : Duplicate Step definitions
	 * Redefined in 'cucumber.eclipse.editor.steps.jdt.JDTStepDefinitions' child class
	 */
	/*@Override
	public void addStepListener(IStepListener listener) {
	    this.listeners.add(listener);
	}*/
	
	/*public void removeStepListener(IStepListener listener) {
	    this.listeners.remove(listener);
	}*/
	
	public void notifyListeners(StepsChangedEvent event) {
        for (IStepListener listener : listeners) {
            listener.onStepsChanged(event);
        }
    }

	
}