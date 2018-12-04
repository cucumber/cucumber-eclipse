package cucumber.eclipse.editor.editors;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import cucumber.eclipse.steps.integration.StepPreferences;

public class StepDefinitionsScanPropertyChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (StepPreferences.PREF_CHECK_STEP_DEFINITIONS.equals(event.getProperty())) {

			IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getEditorReferences();

			boolean checkStepDefinitionsEnabled = StepPreferences.INSTANCE.isCheckStepDefinitionsEnabled();

			for (IEditorReference editorReference : editorReferences) {
				IEditorPart editorPart = editorReference.getEditor(false);
				if (editorPart instanceof Editor) {
					Editor editor = (Editor) editorPart;
					if (checkStepDefinitionsEnabled) {
						editor.refresh();
					} else {
						editor.cleanMarkers();
					}
				}
			}
		}
	}

}
