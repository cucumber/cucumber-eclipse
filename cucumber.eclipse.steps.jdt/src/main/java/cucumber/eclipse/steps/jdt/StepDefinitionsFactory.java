package cucumber.eclipse.steps.jdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class StepDefinitionsFactory implements IExecutableExtensionFactory {

	@Override
	public Object create() throws CoreException {
		return JavaStepDefinitionsProvider.getInstance();
	}
}
