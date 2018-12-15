package cucumber.eclipse.steps.integration;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;

import gherkin.formatter.model.Step;

public class GherkinStepWrapper implements Serializable {

	private static final long serialVersionUID = 1486755494718777426L;
	
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

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
