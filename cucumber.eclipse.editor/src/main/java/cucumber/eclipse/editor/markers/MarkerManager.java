package cucumber.eclipse.editor.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.texteditor.MarkerUtilities;

import cucumber.eclipse.editor.Activator;

public class MarkerManager implements IMarkerManager {

	@Override
	public void add(String type, IFile file, int severity, String message, int lineNumber, int charStart, int charEnd) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.SEVERITY, severity);
		MarkerUtilities.setMessage(attributes, message);
		MarkerUtilities.setLineNumber(attributes, lineNumber);
		MarkerUtilities.setCharStart(attributes, charStart);
		MarkerUtilities.setCharEnd(attributes, charEnd);
		try {
			MarkerUtilities.createMarker(file, attributes, type);
		}
		catch (CoreException exception) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				String.format("Couldn't write marker %s for %s", type, file), exception));
		}
	}

	@Override
	public void removeAll(String type, IFile file) {
		try {
			file.deleteMarkers(type, true, IResource.DEPTH_ZERO);
		}
		catch (CoreException exception) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				String.format("Couldn't remove markers %s from %s", type, file), exception));
		}
	}
}
