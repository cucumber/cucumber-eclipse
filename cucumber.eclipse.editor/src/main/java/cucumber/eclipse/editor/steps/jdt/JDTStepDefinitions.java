package cucumber.eclipse.editor.steps.jdt;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import cucumber.eclipse.editor.preferences.CucumberUserSettingsPage;
import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepsChangedEvent;
import cucumber.eclipse.steps.jdt.StepDefinitions;

/**
 * @author girija.panda@nokia.com
 * 
 *         Purpose: Inhering 'cucumber.eclipse.steps.jdt.StepDefinitions' class
 *         is to avoid plugin-dependency conflicts due to
 *         'CucumberUserSettingsPage' class. Supports reusing of
 *         Step-Definitions from external JAR(.class) exists in class-path.
 *         Reads the package name of external JAR from 'User Settings' of
 *         Cucumber-Preference page and Populate the step proposals from
 *         external JAR
 * 
 *         Also Modified For Issue #211 : Duplicate Step definitions
 * 
 */
public class JDTStepDefinitions extends StepDefinitions implements IStepDefinitions {

	private CucumberUserSettingsPage userSettingsPage = new CucumberUserSettingsPage();
	
	// 1. To get Steps as Set from both Java-Source and JAR file
	@Override
	public Set<Step> getSteps(IFile featurefile, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {

		// Commented By Girija to use LinkedHashSet Instead of HashSet
		// Set<Step> steps = new HashSet<Step>();

		// Used LinkedHashSet : Import all steps from step-definition File
		Set<Step> steps = new LinkedHashSet<Step>();
		try {
			//Scan project and direct referenced projects...
			Set<IProject> projects = new LinkedHashSet<IProject>();
			IProject project = featurefile.getProject();
			projects.add(project);
			projects.addAll(Arrays.asList(project.getReferencedProjects()));
			SubMonitor subMonitor = SubMonitor.convert(progressMonitor, projects.size());
			for (IProject projectToScan : projects) {
				scanProject(projectToScan, featurefile, steps, subMonitor.newChild(1));
			}
		} finally {
			if (progressMonitor != null) {
				 progressMonitor.done();
	         }
		}
		return steps;
	}

	private void scanProject(IProject project, IFile featurefile, Set<Step> steps, IProgressMonitor progressMonitor) throws JavaModelException, CoreException {
		try {
			// Get Package name/s from 'User-Settings' preference
			final String externalPackageName = this.userSettingsPage.getPackageName();
			//System.out.println("Package Names = " + externalPackageName);
			String[] extPackages = externalPackageName.trim().split(COMMA);
			
			//#239:Only match step implementation in same package as feature file
			final boolean onlyPackages = this.userSettingsPage.getOnlyPackages();
			final String onlySpeficicPackagesValue = this.userSettingsPage.getOnlySpecificPackage().trim();
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
							collectCukeStepsFromSource(javaProject, javaPackage, steps, progressMonitor);
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
	public void collectCukeStepsFromSource(IJavaProject javaProject, IPackageFragment javaPackage, Set<Step> steps, IProgressMonitor progressMonitor)
			throws JavaModelException, CoreException {

		for (ICompilationUnit iCompUnit : javaPackage.getCompilationUnits()) {
			// Collect and add Steps
			steps.addAll(getCukeSteps(javaProject, iCompUnit, progressMonitor));
		}
	}

	/**
	 * Collect all cuke-steps from .class file of Jar
	 * 
	 * @param javaPackage
	 * @param steps
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public void collectCukeStepsFromJar(IPackageFragment javaPackage, Set<Step> steps)
			throws JavaModelException, CoreException {

		@SuppressWarnings("deprecation")
		IClassFile[] classFiles = javaPackage.getClassFiles();
		for (IClassFile classFile : classFiles) {
			// System.out.println("----classFile: "
			// +classFile.getElementName());
			steps.addAll(getCukeSteps(javaPackage, classFile));
		}
	}

	
	@Override
	public void addStepListener(IStepListener listener) {
		//this.listeners.add(listener);
		//#240:For Changes in step implementation is reflected in feature file
		StepDefinitions.listeners.add(listener);
	}

	@Override
	public void removeStepListener(IStepListener listener) {
		//this.listeners.remove(listener);
		//#240:For Changes in step implementation is reflected in feature file
		StepDefinitions.listeners.remove(listener);
	}

	public void notifyListeners(StepsChangedEvent event) {
		for (IStepListener listener : listeners) {
			listener.onStepsChanged(event);
		}
	}
}
