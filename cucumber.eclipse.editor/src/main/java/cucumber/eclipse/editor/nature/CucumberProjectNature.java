package cucumber.eclipse.editor.nature;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import cucumber.eclipse.editor.builder.CucumberGherkinBuilder;
import cucumber.eclipse.editor.builder.CucumberStepDefinitionsBuilder;
import cucumber.eclipse.steps.integration.builder.BuilderUtil;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public class CucumberProjectNature implements IProjectNature {
	
	public static final String ID = "cucumber.eclipse.nature";
	
    private IProject project;
    
    @Override
    public void configure() throws CoreException {
    	IProjectDescription description = project.getDescription();
	    String[] oldNatures = description.getNatureIds();
	    String[] newNatures = new String[oldNatures.length+1];
	    newNatures[0] = CucumberProjectNature.ID;
	    for (int it=0; it<oldNatures.length; it++) {
	    	String nature = oldNatures[it];
			if(CucumberProjectNature.ID.equals(nature)) {
				return ; // change nothing
			}
			newNatures[it+1] = nature;
		}
	    description.setNatureIds(newNatures);
	    project.setDescription(description, new NullProgressMonitor());
	    MarkerFactory.INSTANCE.cleanCucumberNatureMissing(project);
    	
        addBuilder(project);
        BuilderUtil.buildProject(project, IncrementalProjectBuilder.FULL_BUILD);
    }

    public void deconfigure() throws CoreException {
    	removeBuilder(project);
    	MarkerFactory.INSTANCE.cleanMarkersRecursively(project);
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
    	ICommand[] buildSpec = description.getBuildSpec();
    	ICommand[] newBuildSpec = new ICommand[buildSpec.length-2];

    	int it = 0;
    	for (ICommand builder : buildSpec) {
			boolean isCucumberBuilder = CucumberStepDefinitionsBuilder.ID.equals(builder.getBuilderName())
					|| CucumberGherkinBuilder.ID.equals(builder.getBuilderName()); 
    		if(!isCucumberBuilder) {
    			newBuildSpec[it++] = builder;
    		}
		}
        
        description.setBuildSpec(newBuildSpec);
        
        project.setDescription(description, new NullProgressMonitor());
    }
}

