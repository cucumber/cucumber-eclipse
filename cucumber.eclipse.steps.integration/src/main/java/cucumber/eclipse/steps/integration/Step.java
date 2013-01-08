package cucumber.eclipse.steps.integration;

import org.eclipse.core.resources.IResource;

public class Step {

	private String text;
	private IResource source;
	private int lineNumber;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public IResource getSource() {
		return source;
	}
	public void setSource(IResource source) {
		this.source = source;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	
}
