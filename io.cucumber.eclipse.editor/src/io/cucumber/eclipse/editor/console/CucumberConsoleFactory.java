package io.cucumber.eclipse.editor.console;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

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
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.showConsoleView(getConsole(consoleManager, always -> true));
	}

	public static CucumberConsole getConsole(boolean show) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		CucumberConsole console = getConsole(consoleManager, CucumberConsole::reset);
		if (show) {
			consoleManager.showConsoleView(console);
		}
		return console;
	}

	private static CucumberConsole getConsole(IConsoleManager consoleManager,
			Predicate<CucumberConsole> filter) {
		return Arrays.stream(consoleManager.getConsoles()).filter(c -> CucumberConsole.TYPE.equals(c.getType()))
				.filter(CucumberConsole.class::isInstance).map(CucumberConsole.class::cast)
				.filter(filter).findFirst().or(() -> {
					CucumberConsole c = new CucumberConsole();
					consoleManager.addConsoles(new IConsole[] { c });
					return Optional.of(c);
				}).get();
	}

}
