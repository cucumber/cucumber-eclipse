package cucumber.eclipse.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class CucumberFeatureLocalApplicationLaunchConfigurationDelegate
extends AbstractJavaLaunchConfigurationDelegate
		implements ILaunchConfigurationDelegate2 {


	@Override
	public void launch(ILaunchConfiguration arg0, String arg1, ILaunch arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub
		
	}
	


}
