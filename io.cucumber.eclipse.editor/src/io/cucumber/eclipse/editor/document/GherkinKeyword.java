package io.cucumber.eclipse.editor.document;

import java.util.Comparator;
import java.util.Locale;

import io.cucumber.gherkin.GherkinDialect;

/**
 * High level representation of a keyword in the gherkin language with some
 * helper methods
 * 
 * @author christoph
 *
 */
public class GherkinKeyword {

	public static final Comparator<? super GherkinKeyword> KEY_ORDER = (w1, w2) -> w1.lcKey.compareTo(w2.lcKey);
	private final String key;
	private final Locale locale;
	private final GherkinDialect dialect;
	private final String lcKey;

	GherkinKeyword(String key, Locale locale, GherkinDialect dialect) {
		this.key = key;
		this.locale = locale;
		this.dialect = dialect;
		lcKey = key.toLowerCase(locale);
	}

	/**
	 * @return the key words string representation
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the locale for this keyword
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @return the dialect this keyword belongs to
	 */
	public GherkinDialect getDialect() {
		return dialect;
	}

	/**
	 * Check if the given string matches the keyword
	 * 
	 * @param str the string to check
	 * @return <code>true</code> if the keyword matches the given string ignoring
	 *         case variants
	 */
	public boolean matches(String str) {
		return str.toLowerCase(locale).equals(lcKey);
	}

	/**
	 * Check if the given string is a prefix of the keyword
	 * 
	 * @param str the string to check
	 * @return <code>true</code> if the given string is a prefix of this keyword
	 *         ignoring case variants
	 */
	public boolean prefix(String str) {
		return lcKey.startsWith(str.toLowerCase(locale));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
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
		GherkinKeyword other = (GherkinKeyword) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
			return false;
		return true;
	}

}
