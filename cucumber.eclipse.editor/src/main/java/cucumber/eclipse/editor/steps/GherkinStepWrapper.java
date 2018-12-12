package cucumber.eclipse.editor.steps;

import org.eclipse.core.resources.IResource;

import gherkin.formatter.model.Step;

public class GherkinStepWrapper {

	private Step step;
	private IResource source;

	public GherkinStepWrapper(Step step, IResource source) {
		super();
		this.step = step;
		this.source = source;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public IResource getSource() {
		return source;
	}

	public void setSource(IResource source) {
		this.source = source;
	}

}
