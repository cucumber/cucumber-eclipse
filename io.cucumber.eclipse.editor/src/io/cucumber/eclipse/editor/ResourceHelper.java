package io.cucumber.eclipse.editor;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ResourceHelper {

	public static IResource find(String path) {
		if (path == null) {
			return null;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.findMember(path);
	}
	
	/**
	 * Collects all feature files in a project, excluding derived resources.
	 * <p>
	 * This method filters out feature files in derived containers such as:
	 * <ul>
	 * <li>Build output directories (target, bin)</li>
	 * <li>Generated source directories</li>
	 * <li>Any other directory marked as derived</li>
	 * </ul>
	 * </p>
	 * 
	 * @param project the project to collect feature files from
	 * @return set of feature files, excluding those in derived containers
	 * @throws CoreException if an error occurs while traversing resources
	 */
	public static Set<IFile> getFeatureFilesInProject(IProject project) throws CoreException {
		Set<IFile> featureFiles = new LinkedHashSet<>();
		
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile file) {
					if ("feature".equals(file.getFileExtension()) && !isDerived(file.getParent())) {
						featureFiles.add(file);
					}
				}
				return true;
			}
		});
		
		return featureFiles;
	}
	
	/**
	 * Checks if a container or any of its parent containers is marked as derived.
	 * <p>
	 * A derived resource is typically a build output or generated file that
	 * should be excluded from processing.
	 * </p>
	 * 
	 * @param container the container to check
	 * @return true if the container or any parent is derived, false otherwise
	 */
	private static boolean isDerived(IContainer container) {
		if (container == null) {
			return false;
		}
		if (container.isDerived()) {
			return true;
		}
		return isDerived(container.getParent());
	}
	
}
