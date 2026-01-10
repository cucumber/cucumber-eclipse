package io.cucumber.eclipse.editor;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;

/**
 * Simple helper class to unify tracing in the cucumber plugin
 * 
 * @author christoph
 *
 */
public class Tracing implements DebugOptionsListener {

	private static final DebugTrace NULL_DEBUG_TRACE = new NullDebugTrace();
	
	// Option path constants
	public static final String PERFORMANCE = "/perf";
	public static final String PERFORMANCE_STEPS = "/perf/steps";
	
	// Debug tracing options
	public static final String DEBUG = "/debug";
	public static final String DEBUG_LAUNCHING = "/debug/launching";
	public static final String DEBUG_VALIDATION = "/debug/validation";
	public static final String DEBUG_VALIDATION_GLUE = "/debug/validation/glue";
	public static final String DEBUG_SEARCH = "/debug/search";
	public static final String DEBUG_STEPS = "/debug/steps";
	
	// Static boolean flags for fast guard checks (updated by optionsChanged)
	public static volatile boolean PERF = false;
	public static volatile boolean PERF_STEPS = false;
	public static volatile boolean DEBUG_ENABLED = false;
	public static volatile boolean DEBUG_LAUNCHING_ENABLED = false;
	public static volatile boolean DEBUG_VALIDATION_ENABLED = false;
	public static volatile boolean DEBUG_VALIDATION_GLUE_ENABLED = false;
	public static volatile boolean DEBUG_SEARCH_ENABLED = false;
	public static volatile boolean DEBUG_STEPS_ENABLED = false;
	
	private static volatile DebugOptions options;

	Tracing() {
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		Tracing.options = options;
		// Update static boolean flags for fast guard checks
		if (options != null) {
			String pluginId = Activator.PLUGIN_ID;
			PERF = options.getBooleanOption(pluginId + PERFORMANCE, false);
			PERF_STEPS = options.getBooleanOption(pluginId + PERFORMANCE_STEPS, false);
			DEBUG_ENABLED = options.getBooleanOption(pluginId + DEBUG, false);
			DEBUG_LAUNCHING_ENABLED = options.getBooleanOption(pluginId + DEBUG_LAUNCHING, false);
			DEBUG_VALIDATION_ENABLED = options.getBooleanOption(pluginId + DEBUG_VALIDATION, false);
			DEBUG_VALIDATION_GLUE_ENABLED = options.getBooleanOption(pluginId + DEBUG_VALIDATION_GLUE, false);
			DEBUG_SEARCH_ENABLED = options.getBooleanOption(pluginId + DEBUG_SEARCH, false);
			DEBUG_STEPS_ENABLED = options.getBooleanOption(pluginId + DEBUG_STEPS, false);
		} else {
			// Reset all flags when options are not available
			PERF = false;
			PERF_STEPS = false;
			DEBUG_ENABLED = false;
			DEBUG_LAUNCHING_ENABLED = false;
			DEBUG_VALIDATION_ENABLED = false;
			DEBUG_VALIDATION_GLUE_ENABLED = false;
			DEBUG_SEARCH_ENABLED = false;
			DEBUG_STEPS_ENABLED = false;
		}
	}

	/**
	 * 
	 * @return the {@link DebugTrace} never <code>null</code>
	 */
	public static DebugTrace get() {
		DebugOptions debugOptions = options;
		if (debugOptions != null && debugOptions.isDebugEnabled()) {
			return debugOptions.newDebugTrace(Activator.PLUGIN_ID);
		}
		return NULL_DEBUG_TRACE;
	}

	private static final class NullDebugTrace implements DebugTrace {

		@Override
		public void trace(String option, String message) {

		}

		@Override
		public void trace(String option, String message, Throwable error) {

		}

		@Override
		public void traceDumpStack(String option) {

		}

		@Override
		public void traceEntry(String option) {

		}

		@Override
		public void traceEntry(String option, Object methodArgument) {

		}

		@Override
		public void traceEntry(String option, Object[] methodArguments) {

		}

		@Override
		public void traceExit(String option) {

		}

		@Override
		public void traceExit(String option, Object result) {

		}

	}

}
