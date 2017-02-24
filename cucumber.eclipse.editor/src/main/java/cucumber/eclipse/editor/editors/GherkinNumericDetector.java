package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.rules.IWordDetector;

public class GherkinNumericDetector implements IWordDetector {

	@Override
	public boolean isWordPart(char ch) {
		return Character.isDigit(ch);
	}

	@Override
	public boolean isWordStart(char ch) {
		return Character.isDigit(ch);
	}

}
