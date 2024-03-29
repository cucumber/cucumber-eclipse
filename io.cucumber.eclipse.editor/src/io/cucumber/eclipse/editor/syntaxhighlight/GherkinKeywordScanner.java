package io.cucumber.eclipse.editor.syntaxhighlight;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;


public class GherkinKeywordScanner extends RuleBasedScanner {
		
    private ColorManager manager;
	
	public GherkinKeywordScanner(ColorManager manager) {
		this.manager = manager;
	}

	public void configureRules(GherkinEditorDocument document) {
		IToken keyword= new Token(new TextAttribute(manager.getColor(GherkinColors.KEYWORD)));
		IToken step= new Token(new TextAttribute(manager.getColor(GherkinColors.STEP)));
		IToken tag= new Token(new TextAttribute(manager.getColor(GherkinColors.TAG)));
		IToken string= new Token(new TextAttribute(manager.getColor(GherkinColors.STRING)));
		IToken comment= new Token(new TextAttribute(manager.getColor(GherkinColors.COMMENT)));
		IToken other= new Token(new TextAttribute(manager.getColor(GherkinColors.DEFAULT)));
		IToken numeric= new Token(new TextAttribute(manager.getColor(GherkinColors.NUMERIC)));
		IToken placeholder= new Token(new TextAttribute(manager.getColor(GherkinColors.PLACEHOLDER)));
		

		List<IRule> rules= new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", comment)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add rule for tags.
		rules.add(new GherkinTagRule(tag));
		
		// Add rule for placeholders.
		rules.add(new GherkinPlaceholderRule(placeholder));
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new GherkinWhitespaceDetector()));


		WordRule wordRule= new WordRule(new GherkinWordDetector(), other);
		WordRule numericRule= new WordRule(new GherkinNumericDetector(), numeric);
		WordRule wordStarStepRule= new WordRule(new GherkinStarStepWordDetector(), step);
		// Add rule to colour the * that can be used instead of steps
		wordStarStepRule.addWord("*", keyword);
		
		document.getTopLevelKeywords()
		.forEach(e -> rules.add(new SingleLineRule(e.getKey().trim() + ":", " ", keyword)));
		document.getFeatureKeywords()
				.forEach(e -> rules.add(new SingleLineRule(e.getKey().trim() + ":", " ", keyword)));
		document.getStepElementKeywords().forEach(e -> rules.add(new SingleLineRule(e.getKey().trim(), " ", step)));
		rules.add(numericRule);
		rules.add(wordRule);
		rules.add(wordStarStepRule);
		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}

	
}
