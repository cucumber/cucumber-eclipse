package io.cucumber.eclipse.java.builder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for removing the Cucumber builder from a project.
 * 
 * @author cucumber-eclipse
 */
public class RemoveCucumberBuilderHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			IProject project = null;

			if (element instanceof IProject) {
				project = (IProject) element;
			} else if (element instanceof IAdaptable) {
				project = ((IAdaptable) element).getAdapter(IProject.class);
			}

			if (project != null) {
				try {
					CucumberFeatureBuilder.removeBuilder(project);
				} catch (CoreException e) {
					throw new ExecutionException("Failed to remove Cucumber builder", e);
				}
			}
		}
		return null;
	}
}
