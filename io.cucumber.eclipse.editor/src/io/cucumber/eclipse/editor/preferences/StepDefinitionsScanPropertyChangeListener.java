package io.cucumber.eclipse.editor.preferences;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


public class StepDefinitionsScanPropertyChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyChanged = event.getProperty();
		
//		if (PREF_CHECK_STEP_DEFINITIONS.equals(propertyChanged)) {
//
//			boolean checkStepDefinitionsEnabled = INSTANCE.isStepDefinitionsMatchingEnabled();
//
//			int buildType = IncrementalProjectBuilder.CLEAN_BUILD;
//			if(checkStepDefinitionsEnabled) {
//				buildType = IncrementalProjectBuilder.FULL_BUILD;
//			}
//			BuilderUtil.buildWorkspace(buildType);
//		}
//		else if (PREF_GLUE_ONLY_IN_SAME_LOCATION.equals(propertyChanged)) {
//			BuilderUtil.buildWorkspace(IncrementalProjectBuilder.FULL_BUILD);
//		}
	}
	
}
