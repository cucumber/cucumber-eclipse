package cucumber.eclipse.steps.integration.filter;

import cucumber.eclipse.steps.integration.StepDefinition;

public class SameLocationFilter implements Filter<StepDefinition> {

	private String location;
	
	public SameLocationFilter(String location) {
		super();
		this.location = location;
	}

	@Override
	public boolean accept(StepDefinition stepDefinition) {
		String stepDefinitionLocation = stepDefinition.getSource().getParent().getFullPath().toString();
		return stepDefinitionLocation.startsWith(location);
	}
}
