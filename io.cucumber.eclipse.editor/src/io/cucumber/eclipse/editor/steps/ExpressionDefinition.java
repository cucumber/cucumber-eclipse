package io.cucumber.eclipse.editor.steps;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.cucumberexpressions.CucumberExpressionParserSupport;
import io.cucumber.cucumberexpressions.CucumberExpressionParserSupport.VariableReplacement;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

/**
 * A Stepexpresion contains the raw unparsed values for a step
 * 
 * @author Christoph Läubrich
 *
 */
public final class ExpressionDefinition {

	private static final Map<Locale, ExpressionFactory> EXPRESSION_FACTORY_MAP = new ConcurrentHashMap<>();

	private final String text;
	private final String lang;

	public ExpressionDefinition(String text) {
		this(text, "");
	}

	@Deprecated
	public ExpressionDefinition(String text, String lang) {
		if (text == null) {
			throw new IllegalArgumentException("text cant be null");
		}
		this.text = text;
		this.lang = lang;
	}

	public String getText() {
		return text;
	}

	@Deprecated
	public String getLang() {
		return lang;
	}

	/**
	 * Test if the expression matches except parameter types, this is done by
	 * converting the expression int a form that accepts any type then perform a
	 * match against the text
	 * 
	 * @param text   the text to check
	 * @param locale the locale to use
	 * @return <code>true</code> if this expression matches <code>false</code>
	 *         otherwise
	 */
	public boolean matchIgnoreTypes(String text, Locale locale) {
		try {
			return EXPRESSION_FACTORY_MAP
					.computeIfAbsent(locale, l -> new ExpressionFactory(new ParameterTypeRegistry(l)))
					.createExpression(
							CucumberExpressionParserSupport.replaceVariables(getText(), VariableReplacement.MATCH_ALL))
					.match(text) != null;
		} catch (RuntimeException e) {
			return false;
		}
	}

	/**
	 * @return the expression text but with all variables replaced
	 */
	public String getTextWithoutVariables() {
		return CucumberExpressionParserSupport.replaceVariables(getText(), VariableReplacement.DELETE);
	}

	@Override
	public String toString() {
		return text + " (" + lang + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionDefinition other = (ExpressionDefinition) obj;
		if (lang == null) {
			if (other.lang != null)
				return false;
		} else if (!lang.equals(other.lang))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

}
