package cucumber.eclipse.editor.markers;

import org.eclipse.core.resources.IFile;

public interface IMarkerManager {
	
	void add(String type, IFile file, int severity, String message, int lineNumber, int charStart, int charEnd);
	
	void removeAll(String type, IFile file);
}
