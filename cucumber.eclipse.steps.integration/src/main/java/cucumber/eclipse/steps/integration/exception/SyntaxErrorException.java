package cucumber.eclipse.steps.integration.exception;

public class SyntaxErrorException extends RuntimeException {

	private static final long serialVersionUID = -253387437746537724L;
	
	private String text;
	private int lineNumber = 0;
	
	public SyntaxErrorException(String text, int lineNumber, Throwable cause) {
		super(cause);
		this.text = text;
		this.lineNumber = lineNumber;
	}

	public String getStepDefinitionText() {
		return text;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
}
