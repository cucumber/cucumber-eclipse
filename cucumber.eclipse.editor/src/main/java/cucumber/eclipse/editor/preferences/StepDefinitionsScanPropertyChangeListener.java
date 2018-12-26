package cucumber.eclipse.editor.preferences;

import static cucumber.eclipse.steps.integration.StepPreferences.INSTANCE;
import static cucumber.eclipse.steps.integration.StepPreferences.PREF_CHECK_STEP_DEFINITIONS;
import static cucumber.eclipse.steps.integration.StepPreferences.PREF_GLUE_ONLY_IN_SAME_LOCATION;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import cucumber.eclipse.steps.integration.builder.BuilderUtil;


public class StepDefinitionsScanPropertyChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyChanged = event.getProperty();
		
		if (PREF_CHECK_STEP_DEFINITIONS.equals(propertyChanged)) {

			boolean checkStepDefinitionsEnabled = INSTANCE.isStepDefinitionsMatchingEnabled();

			int buildType = IncrementalProjectBuilder.CLEAN_BUILD;
			if(checkStepDefinitionsEnabled) {
				buildType = IncrementalProjectBuilder.FULL_BUILD;
			}
			BuilderUtil.buildWorkspace(buildType);
		}
		else if (PREF_GLUE_ONLY_IN_SAME_LOCATION.equals(propertyChanged)) {
			BuilderUtil.buildWorkspace(IncrementalProjectBuilder.FULL_BUILD);
		}
	}
	
}
