package cucumber.eclipse.launching;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

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
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(CucumberFeatureLaunchConstants.CUCUMBER_API_CLI_MAIN, classpath);

		File workingDir = verifyWorkingDirectory(config);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}

		String[] bootpath = getBootpath(config);
		runConfig.setBootClassPath(bootpath);
		
		String featurePath = "" ;
		String gluePath = "";
		boolean isMonochrome = false;
		boolean isPretty = false;
		boolean isProgress= false;
		boolean isJunit= false;
		boolean isJson= false;
		boolean isHtml= false;
		boolean isRerun= false;
		boolean isUsage= false;
		
		featurePath = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePath);
		gluePath  = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePath);
		isMonochrome = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, isMonochrome);
		isPretty = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY,isPretty );
		isProgress = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, isProgress);
		isJunit = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT,isJunit );
		isJson = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON,isJson );
		isHtml = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, isHtml);
		isRerun = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, isRerun);
		isUsage = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE,isUsage );
		
		System.out.println("Launching ....................... " + featurePath);
		System.out.println("Glueing ....................... " + gluePath);
		System.out.println("is monochrome.................." + isMonochrome);
		System.out.println("is pretty.................." + isPretty);
		System.out.println("is progress.................." + isProgress);
		System.out.println("is html.................." + isHtml);
		System.out.println("is json.................." + isJson);
		System.out.println("is junit.................." + isJunit);
		System.out.println("is usage.................." + isUsage);
		System.out.println("is rerun.................." + isRerun);
		
		
		String glue = "--glue";
		String formatter = "--format";
		Collection<String> args = new ArrayList<String>();
		//String[] args = new String[6];
		args.add(featurePath);
		args.add(glue);
		args.add(gluePath);
		
		if (isPretty) {
			args.add(formatter);
			args.add("pretty");	
		}
		
		if (isJson) {
			args.add(formatter);
			args.add("json");	
		}
		
		if (isJunit) {
			args.add(formatter);
			args.add("junit:STDOUT");	
		}
		
		if (isProgress) {
			args.add(formatter);
			args.add("progress");	
		}
		
		if (isRerun) {
			args.add(formatter);
			args.add("rerun");	
		}
		
		if (isHtml) {
			args.add(formatter);
			args.add("html:target");	
		}
		
		if (isUsage) {
			args.add(formatter);
			args.add("usage");	
		}
		
		if (isMonochrome) args.add("--monochrome");
		runConfig.setProgramArguments(args.toArray(new String[0]));

		runner.run(runConfig, launch, monitor);

	}


}
