package cucumber.eclipse.editor.snippet;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getIntegrationExtensionsOfType;

import java.util.List;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.IStepGenerator;

public class ExtensionRegistryStepGeneratorProvider implements IStepGeneratorProvider {
	
	private final List<IStepGenerator> generators = getIntegrationExtensionsOfType(IStepGenerator.class);
	
	@Override
	public IStepGenerator getStepGenerator(IFile targetFile) {
		for (IStepGenerator generator : generators) {
			if (generator.supports(targetFile)) {
				return generator;
			}
		}
		
		return null;
	}
}
