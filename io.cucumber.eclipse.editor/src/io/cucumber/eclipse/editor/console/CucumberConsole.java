package io.cucumber.eclipse.editor.console;

import java.nio.charset.StandardCharsets;

import org.eclipse.ui.console.IOConsole;

import io.cucumber.eclipse.editor.Activator;

public class CucumberConsole extends IOConsole {

	public static final String TYPE = "cucumber";

	public CucumberConsole() {
		super("Cucumber Console", TYPE,
				Activator.getDefault().getImageRegistry().getDescriptor(Activator.ICON_CUKES), StandardCharsets.UTF_8,
				true);
		setWaterMarks(100_000, 1_000_000);
	}

}
