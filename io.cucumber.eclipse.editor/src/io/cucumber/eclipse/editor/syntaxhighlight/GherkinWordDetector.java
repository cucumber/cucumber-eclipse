
package io.cucumber.eclipse.editor.syntaxhighlight;


import org.eclipse.jface.text.rules.IWordDetector;


public class GherkinWordDetector implements IWordDetector {


	public boolean isWordPart(char ch) {
		return Character.isLetterOrDigit(ch);
	}

	public boolean isWordStart(char ch) {
		return Character.isLetterOrDigit(ch);
	}
}
