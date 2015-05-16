package cucumber.eclipse.steps.jdt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ui.IPackagesViewPart;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

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
	    String[] newNatures = new String[oldNatures.length + 1];
	    System.arraycopy(oldNatures, 0, newNatures, 1, oldNatures.length);
	    newNatures[0] = "cucumber.eclipse.steps.jdt.stepsNature"; 
	    description.setNatureIds(newNatures);
	    project.setDescription(description, new NullProgressMonitor());
	    updatePackageExplorer();
    }
    
    private void updatePackageExplorer() {
        final IViewPart foundView = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage()
                        .findView(JavaUI.ID_PACKAGES);

        if (foundView instanceof IPackagesViewPart) {
                final IPackagesViewPart packageExplorerView = (IPackagesViewPart) foundView;
                final TreeViewer treeViewer = packageExplorerView.getTreeViewer();
                treeViewer.refresh();
        }
    }
}
