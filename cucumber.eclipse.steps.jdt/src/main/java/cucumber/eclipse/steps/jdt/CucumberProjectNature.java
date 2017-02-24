package cucumber.eclipse.steps.jdt;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class CucumberProjectNature implements IProjectNature {

    private IProject project;
    
    public void configure() throws CoreException {
        addBuilder(project);
    }

    public void deconfigure() throws CoreException {
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    private void addBuilder(IProject project) throws CoreException {
        IProjectDescription description = project.getDescription();
        ICommand[] oldNatures = description.getBuildSpec();
        ICommand[] newNatures = new ICommand[oldNatures.length + 1];
        System.arraycopy(oldNatures, 0, newNatures, 0, oldNatures.length);
        ICommand newCommand = description.newCommand();
        newCommand.setBuilderName("cucumber.eclipse.steps.jdt.stepsBuilder");
        newNatures[oldNatures.length] = newCommand; 
        description.setBuildSpec(newNatures);
        project.setDescription(description, new NullProgressMonitor());
    }
}
