package cucumber.eclipse.steps.integration.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import cucumber.eclipse.steps.integration.Activator;

public abstract class BuilderUtil {

	public static void buildWorkspace(final int buildType) {
		
		WorkspaceJob job = new WorkspaceJob("Build workspace") {
			IStatus status = new Status(Status.OK, Activator.PLUGIN_ID, "Build successfull");
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					ResourcesPlugin.getWorkspace().build(buildType, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					status = e.getStatus();
				}
				return status;
			}
		};
		
		job.schedule();
		
	}
	

	public static void buildProject(final IProject project, final int buildType) {
		
		WorkspaceJob job = new WorkspaceJob("Build project " + project.getName()) {
			IStatus status = new Status(Status.OK, Activator.PLUGIN_ID, "Build successfull");
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					project.build(buildType, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					status = e.getStatus();
				}
				return status;
			}
		};
		
		job.schedule();
		
	}

	
}
