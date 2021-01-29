package io.cucumber.eclipse.editor.console;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.ui.console.IOConsole;

import io.cucumber.eclipse.editor.Images;

/**
 * a special console type used for cucumber runs
 * 
 * @author christoph
 *
 */
public class CucumberConsole extends IOConsole {

	public static final String TYPE = "cucumber";

	private AtomicBoolean active = new AtomicBoolean(true);

	public CucumberConsole() {
		super("Cucumber Console", TYPE,
				Images.getCukesIconDescriptor(), StandardCharsets.UTF_8,
				true);
		setWaterMarks(100_000, 1_000_000);
	}

	public boolean isActive() {
		return active.get();
	}

	public void setDone() {
		active.set(false);
	}

	boolean reset() {
		boolean reset = active.compareAndSet(false, true);
		if (reset) {
			clearConsole();
		}
		return reset;
	}

}
