package io.cucumber.eclipse.java.launching;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.PrototypeTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaDependenciesTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.JavaRuntime;

public class CucumberFeatureTabGroup extends AbstractLaunchConfigurationTabGroup
		implements ILaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfiguration configuration = DebugUITools.getLaunchConfiguration(dialog);
		boolean isModularConfiguration = configuration != null && JavaRuntime.isModularConfiguration(configuration);
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { //
				new CucumberMainTab(), //
				new JavaArgumentsTab(), //
				new JavaJRETab(), //
				isModularConfiguration ? new JavaDependenciesTab() : new JavaClasspathTab(), //
				// TODO what do we need to support this? new SourceLookupTab(), //
				new EnvironmentTab(), //
				new CommonTab(), //
				new PrototypeTab() };
		setTabs(tabs);
	}
}
