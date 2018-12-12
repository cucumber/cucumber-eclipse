package cucumber.eclipse.editor.preferences;

import static cucumber.eclipse.steps.integration.StepPreferences.INSTANCE;
import static cucumber.eclipse.steps.integration.StepPreferences.PREF_CHECK_STEP_DEFINITIONS;
import static cucumber.eclipse.steps.integration.StepPreferences.PREF_ONLY_SEARCH_PACKAGE;
import static cucumber.eclipse.steps.integration.StepPreferences.PREF_ONLY_SEARCH_SPECIFIC_PACKAGE;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import cucumber.eclipse.editor.editors.Editor;


public class StepDefinitionsScanPropertyChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyChanged = event.getProperty();
		
		
		
		if (PREF_CHECK_STEP_DEFINITIONS.equals(propertyChanged)) {
			// TODO This should be completed by remove or add the StepsBuilder for each projects 
			
			IEditorReference[] editorReferences = editorReferences();

			boolean checkStepDefinitionsEnabled = INSTANCE.isCheckStepDefinitionsEnabled();

			for (IEditorReference editorReference : editorReferences) {
				IEditorPart editorPart = editorReference.getEditor(false);
				if (editorPart instanceof Editor) {
					Editor editor = (Editor) editorPart;
					if (checkStepDefinitionsEnabled) {
//						editor.refresh();
					} else {
						editor.cleanMarkers();
					}
				}
			}
		}
		else if (PREF_ONLY_SEARCH_PACKAGE.equals(propertyChanged) || PREF_ONLY_SEARCH_SPECIFIC_PACKAGE.equals(propertyChanged)) {
			
			IEditorReference[] editorReferences = editorReferences();
			
			for (IEditorReference editorReference : editorReferences) {
				IEditorPart editorPart = editorReference.getEditor(false);
				if (editorPart instanceof Editor) {
					Editor editor = (Editor) editorPart;
//					editor.refresh();
				}
			}
			
		}
	}

	private IEditorReference[] editorReferences() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
	}
	
}
