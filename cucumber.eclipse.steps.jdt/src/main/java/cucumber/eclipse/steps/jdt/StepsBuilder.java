package cucumber.eclipse.steps.jdt;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cucumber.eclipse.steps.integration.StepsChangedEvent;

public class StepsBuilder extends IncrementalProjectBuilder {

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
        StepDefinitions defs = StepDefinitions.getInstance();
        if (defs != null) {
            defs.notifyListeners(new StepsChangedEvent());
        }
        return null;
    }
}
