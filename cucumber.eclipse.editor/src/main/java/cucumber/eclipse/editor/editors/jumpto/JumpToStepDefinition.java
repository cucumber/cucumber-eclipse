package cucumber.eclipse.editor.editors.jumpto;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.steps.integration.StepDefinition;

class JumpToStepDefinition {

	public static void openEditor(StepDefinition stepDefinition) {
		try {
			IResource file = stepDefinition.getSource();
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(IMarker.LINE_NUMBER, stepDefinition.getLineNumber());
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Failed to open step definition %s", stepDefinition.getSource().getFullPath()), e));
		}

	}

}
