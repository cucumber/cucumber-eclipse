package io.cucumber.eclipse.python.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

public class CucumberBehaveLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object element = structuredSelection.getFirstElement();
			IFile file = Adapters.adapt(element, IFile.class);
			if (file != null && "feature".equals(file.getFileExtension())) {
				launchFeatureFile(file, mode);
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IFile file = Adapters.adapt(editor.getEditorInput(), IFile.class);
		if (file != null && "feature".equals(file.getFileExtension())) {
			launchFeatureFile(file, mode);
		}
	}

	private void launchFeatureFile(IFile file, String mode) {
		try {
			ILaunchConfiguration config = findOrCreateLaunchConfiguration(file);
			if (config != null) {
				DebugUITools.launch(config, mode);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private ILaunchConfiguration findOrCreateLaunchConfiguration(IFile file) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager
				.getLaunchConfigurationType(CucumberBehaveLaunchConstants.TYPE_ID);

		// Try to find existing configuration
		ILaunchConfiguration[] configs = launchManager.getLaunchConfigurations(type);
		String featurePath = file.getLocation().toOSString();
		for (ILaunchConfiguration config : configs) {
			String configPath = config.getAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, "");
			if (featurePath.equals(configPath)) {
				return config;
			}
		}

		// Create new configuration
		String configName = generateConfigName(file);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, configName);
		
		// Set attributes
		workingCopy.setAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, featurePath);
		workingCopy.setAttribute(CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, 
				file.getProject().getLocation().toOSString());
		workingCopy.setAttribute(CucumberBehaveLaunchConstants.ATTR_PYTHON_INTERPRETER, "python");
		
		// Set mapped resource for better launch configuration management
		workingCopy.setMappedResources(new IResource[] { file });
		
		return workingCopy.doSave();
	}

	private String generateConfigName(IFile file) {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		String baseName = file.getName().replace(".feature", "");
		return launchManager.generateLaunchConfigurationName(baseName);
	}
}
