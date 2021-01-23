package io.cucumber.eclipse.editor.console;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Manages the console view
 * 
 * @author christoph
 *
 */
public class CucumberConsoleFactory implements IConsoleFactory {

	@Override
	public void openConsole() {
		getConsole(true);
	}

	public static CucumberConsole getConsole(boolean show) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		CucumberConsole console = Arrays.stream(consoleManager.getConsoles())
				.filter(c -> CucumberConsole.TYPE.equals(c.getType())).filter(CucumberConsole.class::isInstance)
				.map(CucumberConsole.class::cast)
				.findAny()
				.or(() -> {
					CucumberConsole c = new CucumberConsole();
					consoleManager.addConsoles(new IConsole[] { c });
					return Optional.of(c);
				}).get();
		if (show) {
			consoleManager.showConsoleView(console);
		}
		return console;
	}

}
