package cucumber.eclipse.steps.integration;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public class ResourceHelper {

	public IResource find(String path) {
		if (path == null) {
			return null;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.findMember(path);
	}
	
}
