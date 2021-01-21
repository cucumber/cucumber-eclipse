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
	public static final String PERFORMANCE = "/perf";

	public static final String PERFORMANCE_STEPS = "/perf/steps";
	private static volatile DebugOptions options;

	Tracing() {
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		Tracing.options = options;
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
