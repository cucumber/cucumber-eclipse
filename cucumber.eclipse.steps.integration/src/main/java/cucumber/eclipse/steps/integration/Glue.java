package cucumber.eclipse.steps.integration;

/**
 * A glue is the link between a gherkin step and its implementation called a
 * step definition.
 * 
 * @author qvdk
 *
 */
public class Glue {

	private GherkinStepWrapper gherkinStepWrapper;
	private StepDefinition stepDefinition;

	public Glue(GherkinStepWrapper gherkinStepWrapper, StepDefinition stepDefinition) {
		super();
		this.gherkinStepWrapper = gherkinStepWrapper;
		this.stepDefinition = stepDefinition;
	}

	public GherkinStepWrapper getGherkinStepWrapper() {
		return gherkinStepWrapper;
	}

	public StepDefinition getStepDefinition() {
		return stepDefinition;
	}

}