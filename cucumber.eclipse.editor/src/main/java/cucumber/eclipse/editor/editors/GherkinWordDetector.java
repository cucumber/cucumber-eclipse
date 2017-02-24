
package cucumber.eclipse.editor.editors;


import org.eclipse.jface.text.rules.IWordDetector;


public class GherkinWordDetector implements IWordDetector {


	public boolean isWordPart(char ch) {
		return Character.isLetterOrDigit(ch);
	}

	public boolean isWordStart(char ch) {
		return Character.isLetterOrDigit(ch);
	}
}
