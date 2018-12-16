package cucumber.eclipse.steps.integration;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public abstract class ResourceUtil {

	public static IResource find(String path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.findMember(path);
	}
	
}
