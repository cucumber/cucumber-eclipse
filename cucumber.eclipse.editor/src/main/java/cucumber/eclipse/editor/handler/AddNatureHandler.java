package cucumber.eclipse.editor.handler;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

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
	    Set<String> natures = new LinkedHashSet<String>(Arrays.asList(oldNatures));
	    natures.add(CucumberProjectNature.ID);
	    description.setNatureIds(natures.toArray(new String[natures.size()]));
	    project.setDescription(description, new NullProgressMonitor());
	    removeCucumberNatureMissingMarkers(project);
    }
    
    private void removeCucumberNatureMissingMarkers(IProject project) throws CoreException {
    	IMarker[] markers = project.findMarkers(CucumberProjectNature.CUCUMBER_NATURE_MISSING_MARKER, false, IResource.DEPTH_ZERO);
	    for (IMarker marker : markers) {
	    	marker.delete();
		}
	    BuilderUtil.buildProject(project, IncrementalProjectBuilder.FULL_BUILD);
    }
}
