package cucumber.eclipse.steps.jdt;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import cucumber.eclipse.steps.integration.StepPreferences;

/**
 * le step builder devrait utiliser le ExtensionRegistryStepProvider.
 * Ce dernier devrait contenir une map avec tous les steps pour un élément donné (IRessource)
 * un IStepDefinition doit pouvoir calculer les steps pour un élément donné et notifier le ExtensionRegistryStepProvider, qui lui même notifierait l'Editor
 * 
 * @author quentin
 *
 */
public class StepsBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "cucumber.eclipse.steps.jdt.stepsBuilder";

	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		if (!StepPreferences.INSTANCE.isCheckStepDefinitionsEnabled()) {
			return null;
		}
		StepDefinitions.getInstance().scan();
//		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
//			fullBuild(monitor);
//		} else {
//			IResourceDelta delta = getDelta(getProject());
//			if (delta == null) {
//				fullBuild(monitor);
//			} else {
//				incrementalBuild(delta, monitor);
//			}
//		}

		return null;
	}

//	private void fullBuild(IProgressMonitor monitor) {
//		StepDefinitions.getInstance().scan();
//	}
//	
//	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
//		IResource updatedResource = delta.getResource();
//		IJavaElement javaElement = JavaCore.create(updatedResource);
//		if(javaElement == null || ! (javaElement instanceof ICompilationUnit) ) {
//			// nothing to do, the JDT plugin supports only Java Elements
//			return ;
//		}
//		
//		IResource resource = javaElement.getResource();
//		if(!(resource instanceof IFile)) {
//			return ;
//		}
//		
//		StepDefinitions.getInstance().scan((IFile) javaElement.getResource());
//	}
}
