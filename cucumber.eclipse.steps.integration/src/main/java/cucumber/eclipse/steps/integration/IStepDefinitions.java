package cucumber.eclipse.steps.integration;

import java.util.Set;

import org.eclipse.core.resources.IFile;

public interface IStepDefinitions {

    void addStepListener(IStepListener listener);

    Set<Step> getSteps(IFile featurefile);

    void removeStepListener(IStepListener listener);
    
    void removeStepListeners();
}
