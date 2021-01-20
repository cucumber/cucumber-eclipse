package io.cucumber.eclipse.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface BuildStorage<T> {

	T getOrCreate(IProject project, IProgressMonitor monitor) throws CoreException;

	void add(IProject project, T glueRepository);

	void persist(IProject project, IProgressMonitor monitor) throws CoreException;

	void load(IProject project, IProgressMonitor monitor) throws CoreException;

}