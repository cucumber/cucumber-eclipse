package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class GherkinPlaceholderRule implements IRule {

	private IToken token;

	public GherkinPlaceholderRule(IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
        int c = scanner.read();
        int count = 1;
        boolean foundId = false;
        if (c == '<') {
        	while ( c != ICharacterScanner.EOF && '\n' != c && '\r' != c  ) {
            	if (!foundId && c != '<' && c != '>' && !Character.isWhitespace(c)) {
            		// any character between the brackets that is non-whitespace is considered as identifier. 
            		foundId = true;
            	}
            	if (c == '<' && count > 1) { // second opening bracket
                    break;
            	}
            	if (c == '>') { // found closing bracket 
            		if (!foundId) { // missing identifier
            			break;
            		}
                    return token;
            	}
                count++;
                c = scanner.read();
            }
        }
        // put the scanner back to the original position if no match
        for( int i = 0; i < count; i++){
        	scanner.unread();
        }
        return Token.UNDEFINED;
	}

}
