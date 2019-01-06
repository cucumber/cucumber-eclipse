package cucumber.eclipse.editor.snippet;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.*;

import java.util.List;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.IStepDefinitionGenerator;

public class ExtensionRegistryStepGeneratorProvider implements IStepGeneratorProvider {
	
	private final List<IStepDefinitionGenerator> generators = getStepDefinitionGenerator();
	
	@Override
	public IStepDefinitionGenerator getStepGenerator(IFile targetFile) {
		for (IStepDefinitionGenerator generator : generators) {
			if (generator.supports(targetFile)) {
				return generator;
			}
		}
		
		return null;
	}
}
