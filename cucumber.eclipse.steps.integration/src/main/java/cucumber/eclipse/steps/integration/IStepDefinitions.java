package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IStepDefinitions {

    void addStepListener(IStepListener listener);

    Set<Step> getSteps(IFile featurefile, IProgressMonitor progressMonitor) throws CoreException;

    void removeStepListener(IStepListener listener);
}
