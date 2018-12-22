package cucumber.eclipse.steps.integration;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;

import gherkin.formatter.model.Step;

/**
 * Convenient class to associate a gherkin step with the file where is come
 * from.
 * 
 * @author qvdk
 *
 */
public class GherkinStepWrapper implements Serializable {

	private static final long serialVersionUID = 1486755494718777426L;

	private Step step;
	private transient IResource source;
	// Since IResource is not serializable, the sourcePath allows 
	// to retrieve the resource after its deserialization.
	// The sourcePath should not be exposed.
	private String sourcePath;

	public GherkinStepWrapper(Step step, IResource source) {
		super();
		this.step = step;
		this.source = source;
		this.sourcePath = source.getFullPath().toString();
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public IResource getSource() {
		if(this.source == null && this.sourcePath != null) {
			source = new ResourceHelper().find(sourcePath);
		}
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
