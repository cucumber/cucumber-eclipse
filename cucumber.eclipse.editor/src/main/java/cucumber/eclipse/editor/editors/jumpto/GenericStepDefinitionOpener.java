package cucumber.eclipse.editor.editors.jumpto;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import cucumber.eclipse.steps.integration.IStepDefinitionOpener;
import cucumber.eclipse.steps.integration.StepDefinition;

public class GenericStepDefinitionOpener implements IStepDefinitionOpener {

	@Override
	public boolean canOpen(StepDefinition stepDefinition) {
		return stepDefinition.getSource() != null && stepDefinition.getLineNumber() > 0;
	}

	@Override
	public void openInEditor(StepDefinition stepDefinition) throws CoreException {
		IResource file = stepDefinition.getSource();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put(IMarker.LINE_NUMBER, stepDefinition.getLineNumber());
		IMarker marker = file.createMarker(IMarker.TEXT);
		marker.setAttributes(map);
		IDE.openEditor(page, marker);
		marker.delete();
	}

}
