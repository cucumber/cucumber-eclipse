package io.cucumber.eclipse.editor;

/**
 * Interface for error logging in the Cucumber Eclipse editor.
 * Implementations can redirect error messages to different destinations.
 */
public interface ErrorLogger {
	
	/**
	 * Log an error message.
	 * 
	 * @param message the message to log
	 */
	void error(String message);
	
	/**
	 * Log an error message with an exception.
	 * 
	 * @param message the message to log
	 * @param throwable the exception to log
	 */
	void error(String message, Throwable throwable);
}
