package io.cucumber.eclipse.editor.steps;

/**
 * A Stepexpresion contains the raw unparsed values for a step
 * 
 * @author Christoph Läubrich
 *
 */
public final class ExpressionDefinition {

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
