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

//	@Override
//	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
////			IProgressMonitor monitor) throws CoreException {
////		// TODO Auto-generated method stub
////		 String mainTypeName = verifyMainTypeName(configuration);
////		    IJavaProject javaProject = getJavaProject(configuration);
////		 //IType type = JavaLaunchConfigurationUtils.getMainType(mainTypeName, javaProject);
////		
////		   // ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
////		    //IType javaLangApplet = JavaLaunchConfigurationUtils.getMainType("java.applet.Applet", javaProject);
//////		 if (!hierarchy.contains(javaLangApplet)) {
//////		        abort("The applet type is not a subclass of java.applet.Applet.", null, 0);
//////		    }
////		 //org.eclipse.debug.core.ILaunchManager.RUN_MODE;
////		 IVMInstall vm = verifyVMInstall(configuration);
////		 IVMRunner runner = vm.getVMRunner(mode);
////		 
////		// Create VM config
////		 VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
////		        "sun.applet.AppletViewer", getClasspath(configuration));
////		   // runConfig.setProgramArguments(new String[] {buildHTMLFile(configuration)});
////		   // String[] vmArgs = execArgs.getVMArgumentsArray();
////		  //  String[] realArgs = new String[vmArgs.length+1];
////		  //  System.arraycopy(vmArgs, 0, realArgs, 1, vmArgs.length);
////		  //  realArgs[0] = javaPolicy;
////		 //runConfig.setVMArguments(realArgs);
////		   
////		    //runConfig.setWorkingDirectory(getDefaultWorkingDirectory(configuration));
////		    // Bootpath
////		    String[] bootpath = getBootpath(configuration);
////		 runConfig.setBootClassPath(bootpath);
////		   
////		    // Launch the configuration
////		   // this.fCurrentLaunchConfiguration = configuration;
////		 runner.run(runConfig, launch, monitor); 
//	}

	@Override
	public void launch(ILaunchConfiguration arg0, String arg1, ILaunch arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub
		
	}
	


}
