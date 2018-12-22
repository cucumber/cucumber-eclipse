package cucumber.eclipse.editor.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import cucumber.eclipse.editor.builder.BuilderUtil;
import cucumber.eclipse.editor.nature.CucumberProjectNature;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;


public class AddNatureHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    Object selection = ((IStructuredSelection)
	            HandlerUtil.getCurrentSelection(event)).getFirstElement();
	    
	    IProject project = (IProject) ((selection instanceof IAdaptable)
	            ? ((IAdaptable) selection).getAdapter(IProject.class) : null);
	    
	    try {
            addNature(project);
        } catch (CoreException e) {
            throw new ExecutionException("Error adding nature", e);
        }
	    
		return null;
	}

    private void addNature(IProject project) throws CoreException {
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
		BuilderUtil.buildProject(project, IncrementalProjectBuilder.FULL_BUILD);

    }
    
   
}
