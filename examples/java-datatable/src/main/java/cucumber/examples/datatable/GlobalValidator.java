package cucumber.examples.datatable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cucumber.plugin.Plugin;

/**
 * This validator is enabled globally in the preferences
 */
public class GlobalValidator implements Plugin {

	private ConcurrentHashMap<Integer, String> errors = new ConcurrentHashMap<>();

	public GlobalValidator() {
		errors.put(1, "Tis is an error from the global validator, but you can't do anything about it!");
	}

	// This is a magic method called by cucumber-eclipse to fetch the final errors and display them in the document
	public Map<Integer, String> getValidationErrors() {
		return errors;
	}

}
