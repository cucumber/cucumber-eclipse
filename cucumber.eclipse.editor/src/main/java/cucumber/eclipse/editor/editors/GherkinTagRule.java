package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class GherkinTagRule implements IRule {

	private IToken token;

	public GherkinTagRule(IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int column = scanner.getColumn();
		if (column >= 1) {
			scanner.unread();
			int pc = scanner.read();
			if (!Character.isWhitespace(pc)) {
				return Token.UNDEFINED;
			}
		}
		int c = scanner.read();
		int count = 1;
		if (c == '@') {
			while (c != ICharacterScanner.EOF) {
				if ( (c == '@' && count > 1) // second @-sign, this can not be a vaild tag
					|| Character.isWhitespace(c)
					) {
					// rewind last read character
					scanner.unread();
					count--;
					break;
				}
				count++;
				c = scanner.read();
			}
			if (count > 2) {
				return token;
			}
		}
		// put the scanner back to the original position if no match
		for (int i = 0; i < count; i++) {
			scanner.unread();
		}
		return Token.UNDEFINED;
	}

}
