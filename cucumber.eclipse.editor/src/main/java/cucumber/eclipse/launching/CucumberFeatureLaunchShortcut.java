package cucumber.eclipse.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.ui.launcher.AbstractLaunchShortcut;
import org.eclipse.ui.IEditorPart;

public class CucumberFeatureLaunchShortcut extends AbstractLaunchShortcut implements ILaunchShortcut2 {

	private String newLaunchConfigurationName;


	@Override
	public void launch(ISelection selection, String arg1) {
		// TODO Auto-generated method stub
		
	}

  	@Override
	public void launch(IEditorPart part, String mode) {
  		newLaunchConfigurationName = part.getTitle();
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
		IProject project = CucumberFeatureLaunchUtils.getProject();
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, CucumberFeatureLaunchUtils.getFeaturePath());
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, CucumberFeatureLaunchConstants.DEFAULT_CLASSPATH);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, true);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY, true);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, false);
		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, false);
		
	}
	
	@Override
	protected String getName(ILaunchConfigurationType type) {
		if(newLaunchConfigurationName != null) {
			return newLaunchConfigurationName;
		}
		return super.getName(type);
	}

	
	@Override
	protected boolean isGoodMatch(ILaunchConfiguration configuration) {
		boolean goodType = isGoodType(configuration);
		boolean goodName = isGoodName(configuration);
		return goodType && goodName;
	}

	private boolean isGoodName(ILaunchConfiguration configuration) {
		return configuration.getName().equals(newLaunchConfigurationName);
	}

	private boolean isGoodType(ILaunchConfiguration configuration) {
		try {
			String identifier = configuration.getType().getIdentifier();
			return CucumberFeatureLaunchConstants.CUCUMBER_FEATURE_LAUNCH_CONFIG_TYPE.equals(identifier);
		} catch (CoreException e) {
			return false;
		}
	}

}
