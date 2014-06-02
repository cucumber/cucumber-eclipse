package cucumber.eclipse.steps.integration;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;

public class Step {

	private String text;
	private IResource source;
	private int lineNumber;
	private Pattern compiledText;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
		this.compiledText = Pattern.compile(text);
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
	
	public boolean matches(String s) {
		return compiledText.matcher(s).find();
	}
	
	@Override
	public String toString() {
		return "Step [text=" + text + ", source=" + source + ", lineNumber="
				+ lineNumber + "]";
	}
}
