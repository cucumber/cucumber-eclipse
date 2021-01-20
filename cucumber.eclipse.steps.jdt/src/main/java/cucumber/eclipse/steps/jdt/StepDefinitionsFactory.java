package cucumber.eclipse.steps.jdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import io.cucumber.eclipse.java.JavaStepDefinitionsProvider;

public class StepDefinitionsFactory implements IExecutableExtensionFactory {

	@Override
	public Object create() throws CoreException {
		return JavaStepDefinitionsProvider.getInstance();
	}
}
