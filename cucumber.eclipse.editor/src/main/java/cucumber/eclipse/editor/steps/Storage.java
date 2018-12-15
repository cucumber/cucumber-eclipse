package cucumber.eclipse.editor.steps;

import org.eclipse.core.resources.IProject;

public interface Storage<T> {

	T getOrCreate(IProject project);

	void add(IProject project, T glueRepository);

}