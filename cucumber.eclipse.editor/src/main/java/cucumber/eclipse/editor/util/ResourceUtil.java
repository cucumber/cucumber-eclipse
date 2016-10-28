package cucumber.eclipse.editor.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/*
 * Used to read any properties file as key name and value
 */
public class ResourceUtil {

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("cucumber.eclipse.editor.<PropertiesFilename>");

	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException localMissingResourceException) {
		}
		return '!' + key + '!';
	}

}
