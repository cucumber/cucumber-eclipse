package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.rules.IWordDetector;

public class GherkinStarStepWordDetector implements IWordDetector {

	@Override
	public boolean isWordStart(char c) {
		return c=='*';
	}

	@Override
	public boolean isWordPart(char c) {
		return false;
	}

}
