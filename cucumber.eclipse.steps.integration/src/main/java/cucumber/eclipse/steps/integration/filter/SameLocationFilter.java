package cucumber.eclipse.steps.integration.filter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

import io.cucumber.eclipse.editor.steps.StepDefinition;

public class SameLocationFilter implements Filter<StepDefinition> {

	private String location;
	
	public SameLocationFilter(String location) {
		super();
		this.location = location;
	}

	@Override
	public boolean accept(StepDefinition stepDefinition) {
		IResource source = stepDefinition.getSource();
		if (source == null) {
			return false;
		}
		IContainer parent = source.getParent();
		if (parent == null) {
			return false;
		}
		String stepDefinitionLocation = String.valueOf(parent.getFullPath());
		return stepDefinitionLocation.startsWith(location);
	}
}
