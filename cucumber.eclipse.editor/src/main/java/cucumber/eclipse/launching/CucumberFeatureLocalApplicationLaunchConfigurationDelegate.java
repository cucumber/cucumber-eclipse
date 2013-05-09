package cucumber.eclipse.launching;

import java.io.File;

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

public class CucumberFeatureLocalApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate implements ILaunchConfigurationDelegate2 {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IVMInstall vm = verifyVMInstall(config);
		IVMRunner runner = vm.getVMRunner(mode);
		String[] classpath = getClasspath(config);
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration("cucumber.api.cli.Main", classpath);

		File workingDir = verifyWorkingDirectory(config);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}

		String[] bootpath = getBootpath(config);
		runConfig.setBootClassPath(bootpath);
		
		String featurePath = "" ;
		String gluePath = "";
		
		featurePath = config.getAttribute("cucumber feature", featurePath);
		gluePath  = config.getAttribute("glue path", gluePath);
		System.out.println("Launching ....................... " + featurePath);
		System.out.println("Glueing ....................... " + gluePath);
		
		
		String glue = "--glue";
		String[] args = new String[3];

		args[0] = featurePath;
		args[1] = glue;
		args[2] = gluePath;
		runConfig.setProgramArguments(args);

		runner.run(runConfig, launch, monitor);

	}

}
