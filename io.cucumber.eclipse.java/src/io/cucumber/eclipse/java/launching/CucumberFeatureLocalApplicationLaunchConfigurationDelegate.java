package io.cucumber.eclipse.java.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.osgi.framework.FrameworkUtil;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.eclipse.editor.console.CucumberConsole;
import io.cucumber.eclipse.editor.console.CucumberConsoleFactory;
import io.cucumber.eclipse.editor.launching.ILauncher.Mode;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberEclipsePlugin;
import io.cucumber.eclipse.java.plugins.MessageEndpoint;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.tagexpressions.Expression;
import mnita.ansiconsole.preferences.AnsiConsolePreferenceUtils;

public class CucumberFeatureLocalApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate
		implements ILaunchConfigurationDelegate2 {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		if (config.getAttribute(CucumberFeatureLaunchConstants.ATTR_INTERNAL_LAUNCHER,
				CucumberFeatureLaunchConstants.DEFAULT_INTERNAL_LAUNCHER)) {
			runEmbedded(config, mode, launch, monitor);
			return;
		}

		IVMInstall vm = verifyVMInstall(config);
		IVMRunner runner = vm.getVMRunner(mode);
		String[][] classpathAndModules = getClasspathAndModulepath(config);
		VMRunnerConfiguration runConfig = createRunConfig(classpathAndModules);

		verifyWorkingDirectory(config);

		String[] bootpath = getBootpath(config);
		runConfig.setBootClassPath(bootpath);
		String[] modulepath = classpathAndModules[1];
		runConfig.setModulepath(modulepath);
		runConfig.setVMArguments(DebugPlugin.parseArguments(getVMArguments(config)));
		runConfig.setWorkingDirectory(getWorkingDirectory(config).getAbsolutePath());

		String featurePath = "";
		String gluePath = "";
		boolean isMonochrome = false;
		boolean isPretty = false;
		boolean isProgress = false;
		boolean isJunit = false;
		boolean isJson = false;
		boolean isHtml = false;
		boolean isRerun = false;
		boolean isUsage = false;

		featurePath = substituteVar(config.getAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePath));
		gluePath = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePath);
		isMonochrome = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, isMonochrome);
		isPretty = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY, isPretty);
		isProgress = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, isProgress);
		isJunit = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, isJunit);
		isJson = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, isJson);
		isHtml = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, isHtml);
		isRerun = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, isRerun);
		isUsage = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, isUsage);

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
		String formatter = "--plugin"; // Cucumber-JVM's --format option is deprecated. Please use --plugin instead.
		Collection<String> args = new ArrayList<String>();
		// String[] args = new String[6];
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
		if (!AnsiConsolePreferenceUtils.isAnsiConsoleEnabled()) {
			args.add("--monochrome");
		}
		MessageEndpoint endpoint;
		try {
			endpoint = new MessageEndpoint(envelope -> {
				// TODO publish!
			});
			endpoint.addArguments(args);
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, getClass(), "Can't create remote process communication channel", e));
		}
		try {
			args.addAll(Arrays.asList(DebugPlugin.parseArguments(getProgramArguments(config))));
			String[] finalArgs = args.toArray(new String[0]);
			runConfig.setProgramArguments(finalArgs);
			endpoint.start();
			runner.run(runConfig, launch, monitor);
		} catch (CoreException core) {
			endpoint.close();
			throw core;
		} catch (RuntimeException runtime) {
			endpoint.close();
			throw runtime;
		}

	}

	private void runEmbedded(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		IJavaProject project = JDTUtil.getJavaProject(projectName);
		if (project == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, getClass(), "Project with name '" + projectName + "' not found"));
		}
		String featurePath = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, "");
		IResource resource = project.getProject().findMember(featurePath);
		if (resource instanceof IFile) {
			Optional<Feature> feature = CucumberRuntime.loadFeature(new FileResource((IFile) resource));
			if (feature.isPresent()) {
				// TODO read from config...
				List<FeatureWithLines> featureFilter = new ArrayList<>();
				ArrayList<Expression> tagFilters = new ArrayList<>();
				try (CucumberConsole console = CucumberConsoleFactory.getConsole(true)) {
					CucumberRuntimeLauncher.runFeaturesEmbedded(project, Collections.singletonList(feature.get()),
							featureFilter, Mode.parseString(mode),
							console, monitor, tagFilters);
				}
			} else {
				throw new CoreException(
						new Status(IStatus.ERROR, getClass(), "no feature present in '" + featurePath + "'."));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, getClass(), "feature '" + featurePath + "' not found"));
		}
	}

	private VMRunnerConfiguration createRunConfig(String[][] classpathAndModules) {
		List<String> classPath = new ArrayList<>(Arrays.asList(classpathAndModules[0]));
		try {
			File file = FileLocator.getBundleFile(FrameworkUtil.getBundle(CucumberEclipsePlugin.class));
			if (file != null) {
				if (file.isDirectory() && !new File(file, "io").exists()) {
					// try to get the path for the IDE...
					File binDirectory = new File(file, "bin");
					if (binDirectory.exists()) {
						file = binDirectory;
					} else {
						File targetDirectory = new File(file, "target/classes");
						if (targetDirectory.exists()) {
							file = targetDirectory;
						}
					}
				}
				classPath.add(file.getAbsolutePath());
			}
		} catch (IOException e1) {
		}
		String[] finalClassPath = classPath.toArray(String[]::new);
		for (String string : finalClassPath) {
			System.out.println(string);
		}
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
				CucumberFeatureLaunchConstants.CUCUMBER_API_CLI_MAIN, finalClassPath);
		return runConfig;
	}

	/**
	 * Substitute any variable
	 */
	private static String substituteVar(String s) {
		if (s == null) {
			return s;
		}
		try {
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(s);
		} catch (CoreException e) {
			System.out.println("Could not substitute variable " + s);
			return null;
		}
	}

}
