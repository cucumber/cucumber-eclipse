package cucumber.eclipse.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.internal.launching.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.ui.launcher.AbstractLaunchShortcut;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public class CucumberFeatureLaunchShortcut extends AbstractLaunchShortcut implements ILaunchShortcut2 {

	@Override
	public void launch(ISelection selection, String arg1) {
		// TODO Auto-generated method stub
		
	}

  	@Override
	public void launch(IEditorPart part, String mode) {
		launch(mode);
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getLaunchConfigurationTypeName() {
		return CucumberFeatureLaunchConstants.CUCUMBER_FEATURE_LAUNCH_CONFIG_TYPE;
	}

	@Override
	protected void initializeConfiguration(ILaunchConfigurationWorkingCopy config) {
		IProject project = CucumberFeaureLaunchUtils.getProject();
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, CucumberFeaureLaunchUtils.getFeaturePath());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, CucumberFeatureLaunchConstants.DEFAULT_CLASSPATH);
	}

	
	@Override
	protected boolean isGoodMatch(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		return false;
	}


}
