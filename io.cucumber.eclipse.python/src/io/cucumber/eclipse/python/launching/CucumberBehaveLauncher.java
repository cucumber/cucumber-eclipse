package io.cucumber.eclipse.python.launching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.editor.launching.Mode;
import io.cucumber.messages.types.Scenario;
import io.cucumber.tagexpressions.Expression;

/**
 * Launches Cucumber feature files using Python's Behave framework
 * 
 * @author copilot
 */
@Component(service = ILauncher.class)
public class CucumberBehaveLauncher implements ILauncher {

	@Override
	public void launch(Map<GherkinEditorDocument, IStructuredSelection> launchMap, Mode mode, boolean temporary,
			IProgressMonitor monitor) throws CoreException {
		
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = lm.getLaunchConfigurationType(CucumberBehaveLaunchConstants.TYPE_ID);
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, launchMap.size() * 100);
		
		for (Entry<GherkinEditorDocument, IStructuredSelection> entry : launchMap.entrySet()) {
			GherkinEditorDocument document = entry.getKey();
			IResource resource = document.getResource();
			IProject project = resource.getProject();
			
			if (project == null || !supports(resource)) {
				continue;
			}
			
			ILaunchConfiguration lc = getLaunchConfiguration(project, resource, type);
			String identifier = mode.getLaunchMode().getIdentifier();
			
			if (temporary) {
				ILaunchConfigurationWorkingCopy copy = lc.getWorkingCopy();
				
				// Handle scenarios with specific line numbers
				List<Integer> lines = new ArrayList<>();
				List<Expression> tagFilters = new ArrayList<>();
				
				IStructuredSelection selection = entry.getValue();
				for (Object object : selection) {
					if (object instanceof Scenario) {
						Scenario scenario = (Scenario) object;
						lines.add(scenario.getLocation().getLine().intValue());
					} else if (object instanceof Expression) {
						tagFilters.add((Expression) object);
					}
				}
				
				// Set feature path with line numbers if any scenarios are selected
				if (!lines.isEmpty()) {
					String featurePathWithLine = resource.getLocation().toOSString() + ":" + 
						lines.stream().map(String::valueOf).collect(Collectors.joining(":"));
					copy.setAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_WITH_LINE, featurePathWithLine);
				}
				
				// Set tag filters if any
				if (!tagFilters.isEmpty()) {
					String tags = tagFilters.stream()
							.map(Expression::toString)
							.collect(Collectors.joining(" and "));
					copy.setAttribute(CucumberBehaveLaunchConstants.ATTR_TAGS, tags);
				}
				
				copy.launch(identifier, subMonitor.split(100));
			} else {
				lc.launch(identifier, subMonitor.split(100));
			}
		}
	}

	private ILaunchConfiguration getLaunchConfiguration(IProject project, IResource resource,
			ILaunchConfigurationType type) throws CoreException {
		
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		String featurePath = resource.getLocation().toOSString();
		
		// Try to find existing configuration
		for (ILaunchConfiguration configuration : lm.getLaunchConfigurations(type)) {
			String configPath = configuration.getAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, "");
			if (featurePath.equals(configPath)) {
				return configuration;
			}
		}
		
		// Create new configuration
		ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
				lm.generateLaunchConfigurationName(resource.getName()));
		
		wc.setAttribute(CucumberBehaveLaunchConstants.ATTR_FEATURE_PATH, featurePath);
		wc.setAttribute(CucumberBehaveLaunchConstants.ATTR_WORKING_DIRECTORY, 
				project.getLocation().toOSString());
		wc.setAttribute(CucumberBehaveLaunchConstants.ATTR_PYTHON_INTERPRETER, "python");
		
		// Set mapped resource for better launch configuration management
		wc.setMappedResources(new IResource[] { resource });
		
		return wc.doSave();
	}

	@Override
	public boolean supports(IResource resource) {
		return BehaveProcessLauncher.isBehaveProject(resource);
	}

	@Override
	public boolean supports(Mode mode) {
		// For now, only support RUN mode. Debug support can be added later.
		return mode == Mode.RUN;
	}

}
