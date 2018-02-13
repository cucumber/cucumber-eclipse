package cucumber.eclipse.editor.steps.jdt;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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

	// To Collect all Steps as Set for ContentAssistance
	public static Set<Step> steps = null;

	private CucumberUserSettingsPage userSettingsPage = new CucumberUserSettingsPage();

	
	// 1. To get Steps as Set from both Java-Source and JAR file
	@Override
	public Set<Step> getSteps(IFile featurefile) {
		
		// Commented By Girija to use LinkedHashSet Instead of HashSet
		// Set<Step> steps = new HashSet<Step>();
		
		//Used LinkedHashSet : Import all steps from step-definition File
		steps = new LinkedHashSet<Step>();
		IProject project = featurefile.getProject();

		String featurefilePackage = featurefile.getParent().getFullPath().toString();

		try {

			if (project.isNatureEnabled(JAVA_PROJECT)) {

				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();

				// Get Package name
				final String externalPackageName = this.userSettingsPage.getPackageName();
				final boolean onlyPackages = this.userSettingsPage.getOnlyPackages();

				for (IPackageFragment javaPackage : packages) {

					// Get Packages from source folder of current project
					if ((javaPackage.getKind() == JAVA_SOURCE && (!onlyPackages || featurefilePackage.startsWith(javaPackage.getPath().toString())))) {
						// System.out.println("Package Name-1 :"+javaPackage.getElementName());

						// Collect All Steps From Source
						collectCukeStepsFromSource(javaProject, javaPackage, steps);
					}

					
					// Get Packages from JAR exists in class-path
					if ((javaPackage.getKind() == JAVA_JAR_BINARY) && !externalPackageName.equals("")) {

						// Check package from external JAR/class file
						if (javaPackage.getElementName().equals(externalPackageName)
								|| javaPackage.getElementName().startsWith(externalPackageName)) {
							// Collect All Steps From JAR
							collectCukeStepsFromJar(javaPackage, steps);
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return steps;
	}

	/**Collect all cuke-steps from java-source Files
	 * @param javaProject
	 * @param javaPackage
	 * @param steps
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public void collectCukeStepsFromSource(IJavaProject javaProject, IPackageFragment javaPackage, Set<Step> steps)
			throws JavaModelException, CoreException {

		for (ICompilationUnit iCompUnit : javaPackage.getCompilationUnits()) {			
			// Collect and add Steps
			steps.addAll(getCukeSteps(javaProject, iCompUnit));
		}
	}
	
	/**Collect all cuke-steps from .class file of Jar
	 * @param javaPackage
	 * @param steps
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public void collectCukeStepsFromJar(IPackageFragment javaPackage, Set<Step> steps)
			throws JavaModelException, CoreException {

		IClassFile[] classFiles = javaPackage.getClassFiles();
		for (IClassFile classFile : classFiles) {
			//System.out.println("----classFile: " +classFile.getElementName());
			steps.addAll(getCukeSteps(javaPackage, classFile));
		}
	}

	@Override
	public void addStepListener(IStepListener listener) {
		StepDefinitions.listeners.add(listener);
	}

	@Override
	public void removeStepListener(IStepListener listener) {
		StepDefinitions.listeners.remove(listener);
	}

	public void notifyListeners(StepsChangedEvent event) {
		for (IStepListener listener : listeners) {
			listener.onStepsChanged(event);
		}
	}
}
