package io.cucumber.eclipse.editor.debug;

import org.eclipse.debug.core.DebugException;

/**
 * a runnable that can throw a {@link DebugException}
 * 
 * @author christoph
 *
 */
public interface DebugRunnable {

	void run() throws DebugException;
}
