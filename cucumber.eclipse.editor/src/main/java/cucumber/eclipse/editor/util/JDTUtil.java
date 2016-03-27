package cucumber.eclipse.editor.util;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class JDTUtil {

	public JDTUtil() {
		// TODO Auto-generated constructor stub
	}

	public static IJavaProject getJavaProject(String projectName) {
		if ((projectName == null) || (projectName.length() < 1)) {
			return null;
		}
		return getJavaModel().getJavaProject(projectName);
	}

	public static IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	
}
