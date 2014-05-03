package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.Step;

public class StepDefinitions implements IStepDefinitions {

	private Pattern cukeAnnotationMatcher = Pattern.compile("cucumber\\..*\\.(Given|When|Then|And|But)");
	
	@Override
	public Set<Step> getSteps(IProject project) {
		Set<Step> steps = new HashSet<Step>();

		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);

				IPackageFragment[] packages = javaProject.getPackageFragments();
				for (IPackageFragment javaPackage : packages) {

					if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

						System.out.println("Package "
								+ javaPackage.getElementName());

						for (ICompilationUnit compUnit : javaPackage
								.getCompilationUnits()) {
							steps.addAll(getCukeAnnotations(compUnit));
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} 
		return steps;
	}
	
	private List<Step> getCukeAnnotations(ICompilationUnit compUnit)
			throws JavaModelException{
		List<Step> steps = new ArrayList<Step>();
		
		List<String> importedAnnotations = new ArrayList<String>();
		
		for (IImportDeclaration decl : compUnit.getImports()) {
			Matcher m = cukeAnnotationMatcher.matcher(decl.getElementName());
			if (m.find()) {
				importedAnnotations.add(m.group(1));
			}
		}
		
		for (IType t : compUnit.getTypes()) {
			for (IMethod method : t.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {
					if (isStepAnnotation(importedAnnotations, annotation)) {
						Step step = new Step();
						step.setSource(method.getResource());
						step.setText(getAnnotationText(annotation));
						step.setLineNumber(getLineNumber(compUnit, annotation));
						steps.add(step);

					}
				}
			}
		}
		System.out.println(steps);
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
	
	private boolean isStepAnnotation(List<String> importedAnnotations,
			IAnnotation annotation) throws JavaModelException {
		
		if (cukeAnnotationMatcher.matcher(annotation.getElementName()).find()) return true;
		if (importedAnnotations.contains(annotation.getElementName())) {
			return true;
		}
		return false; 
	}

	private String getAnnotationText(IAnnotation annotation) throws JavaModelException {
		for (IMemberValuePair mvp :  annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}

}
