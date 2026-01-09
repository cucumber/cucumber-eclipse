package io.cucumber.eclipse.editor;

import org.eclipse.core.runtime.ILog;

/**
 * Unified error logging facility for the Cucumber Eclipse editor.
 * Provides static methods to log error messages at different severity levels.
 * By default, logs to Eclipse's ILog facility, but can be redirected
 * to a custom EditorErrorLog implementation for testing or alternative
 * logging destinations.
 * 
 * For debug and informational output, use the {@link Tracing} facility instead,
 * which integrates with Eclipse's standard OSGi tracing mechanism.
 */
public final class EditorLogging {
	
	private static volatile ErrorLogger errorLog;
	
	private EditorLogging() {
		// Utility class
	}
	
	/**
	 * Sets a custom error log to redirect all error messages.
	 * If set to null, logging will fall back to Eclipse's ILog facility.
	 * 
	 * @param customErrorLog the custom error log, or null to use default ILog
	 */
	public static void setErrorLog(ErrorLogger customErrorLog) {
		errorLog = customErrorLog;
	}
	
	/**
	 * Log an error message.
	 * 
	 * @param message the message to log
	 */
	public static void error(String message) {
		ErrorLogger currentLog = errorLog;
		if (currentLog != null) {
			currentLog.error(message);
		} else {
			ILog.get().error(message);
		}
	}
	
	/**
	 * Log an error message with an exception.
	 * 
	 * @param message the message to log
	 * @param throwable the exception to log
	 */
	public static void error(String message, Throwable throwable) {
		ErrorLogger currentLog = errorLog;
		if (currentLog != null) {
			currentLog.error(message, throwable);
		} else {
			ILog.get().error(message, throwable);
		}
	}
}
