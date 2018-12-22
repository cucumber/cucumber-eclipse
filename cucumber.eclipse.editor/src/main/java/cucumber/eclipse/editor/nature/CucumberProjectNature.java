package cucumber.eclipse.editor.nature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import cucumber.eclipse.editor.builder.CucumberGherkinBuilder;
import cucumber.eclipse.editor.builder.CucumberStepDefinitionsBuilder;

public class CucumberProjectNature implements IProjectNature {
	
	public static final String ID = "cucumber.eclipse.nature";
	
    private IProject project;
    
    public void configure() throws CoreException {
        addBuilder(project);
//        IProject[] projects = project.getReferencedProjects();
//    	for (IProject referencedProject : projects) {
//    		addBuilder(referencedProject);
//		}
    }

    public void deconfigure() throws CoreException {
    	removeBuilder(project);
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    private void addBuilder(IProject projectToUpdate) throws CoreException {
        IProjectDescription description = projectToUpdate.getDescription();
        
        // Avoid using a Set since ICommand does not have 
        // a hash + equalTo methods to avoid duplicates
        // So we will filter by id
        Map<String, ICommand> builders = new LinkedHashMap<String, ICommand>(description.getBuildSpec().length + 1);
        for (ICommand builder : description.getBuildSpec()) {
			builders.put(builder.getBuilderName(), builder);
		}
        
        // First build step definitions
        ICommand stepDefinitionsBuilder = description.newCommand();
        stepDefinitionsBuilder.setBuilderName(CucumberStepDefinitionsBuilder.ID);
        builders.put(stepDefinitionsBuilder.getBuilderName(), stepDefinitionsBuilder);
        
        // Then build gherkins
        ICommand gherkinBuilder = description.newCommand();
        gherkinBuilder.setBuilderName(CucumberGherkinBuilder.ID);
        builders.put(gherkinBuilder.getBuilderName(), gherkinBuilder);
        
        description.setBuildSpec(builders.values().toArray(new ICommand[builders.size()]));
        projectToUpdate.setDescription(description, new NullProgressMonitor());
    }
    
    private void removeBuilder(IProject project) throws CoreException {
    	IProjectDescription description = project.getDescription();
    	Set<ICommand> builders = new LinkedHashSet<ICommand>(Arrays.asList(description.getBuildSpec()));

    	Set<ICommand> toRemove = new HashSet<ICommand>();
        for (ICommand builder : builders) {
			if(CucumberStepDefinitionsBuilder.ID.equals(builder.getBuilderName())) {
				toRemove.remove(builder);
			}
		}
        builders.removeAll(toRemove);
        
        description.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
        
        project.setDescription(description, new NullProgressMonitor());
    }
}

