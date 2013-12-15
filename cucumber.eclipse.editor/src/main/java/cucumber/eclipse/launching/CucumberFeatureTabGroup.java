package cucumber.eclipse.launching;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaConnectTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LocalJavaApplicationTabGroup;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.swt.widgets.Composite;

public class CucumberFeatureTabGroup 
	extends AbstractLaunchConfigurationTabGroup
	implements ILaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
		 		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
		 		new CucumberMainTab(),
				//new JavaArgumentsTab(),
				new JavaJRETab(),
				new JavaClasspathTab(), 
				new CommonTab()
		};
		setTabs(tabs);
	}
}


