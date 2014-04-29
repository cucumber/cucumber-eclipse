package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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

import cucumber.eclipse.steps.integration.Step;


public class StepHandler extends AbstractHandler {

	private List<Step> steps = new ArrayList<>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Get the root of the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			try {
				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
					IJavaProject javaProject = JavaCore.create(project);

					IPackageFragment[] packages = javaProject
							.getPackageFragments();
					for (IPackageFragment javaPackage : packages) {

						if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

							System.out.println("Package "
									+ javaPackage.getElementName());

							for (ICompilationUnit compUnit : javaPackage
									.getCompilationUnits()) {
								getCukeAnnotations(compUnit);
							}
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void getCukeAnnotations(ICompilationUnit compUnit)
			throws JavaModelException {
		for (IType t : compUnit.getTypes()) {
			for (IMethod method : t.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {
					if (isStepAnnotation(compUnit, annotation)) {
						Step step = new Step();
						step.setSource(method.getResource());
						step.setText(getAnnotationText(annotation));

						steps.add(step);

					}
				}
			}
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
