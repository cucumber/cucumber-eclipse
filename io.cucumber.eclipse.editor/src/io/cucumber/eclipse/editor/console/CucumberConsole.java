package io.cucumber.eclipse.editor.console;

import java.nio.charset.StandardCharsets;

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

	public CucumberConsole() {
		super("Cucumber Console", TYPE,
				Images.getCukesIconDescriptor(), StandardCharsets.UTF_8,
				true);
		setWaterMarks(100_000, 1_000_000);
	}

}
