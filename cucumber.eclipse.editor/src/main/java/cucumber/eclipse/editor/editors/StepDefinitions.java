package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
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
		
		for (IType t : compUnit.getTypes()) {
			for (IMethod method : t.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {
					if (isStepAnnotation(compUnit, annotation)) {
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
	
	private boolean isStepAnnotation(ICompilationUnit compUnit,
			IAnnotation annotation) {
		
		List<String>  annotations = Arrays.asList("Given", "When", "Then");
		List<String>  fqAnnotations = Arrays.asList("cucumber.annotation.Given", "cucumber.annotation.When", "cucumber.annotation.Then");
		
		if (fqAnnotations.contains(annotation.getElementName())) return true;
		if (annotations.contains(annotation.getElementName())) {
			// TODO: Check imports
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
