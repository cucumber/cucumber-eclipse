package cucumber.eclipse.steps.jdt.ui;

import static cucumber.eclipse.steps.jdt.ui.CucumberJavaPreferences.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import cucumber.eclipse.steps.integration.builder.BuilderUtil;


public class CucumberJavaPreferencesChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyChanged = event.getProperty();

		List<String> listenedPreferences = Arrays.asList(PREF_USE_STEP_DEFINITIONS_FILTERS, PREF_ACTIVE_FILTERS_LIST, PREF_INACTIVE_FILTERS_LIST);
		
		if(listenedPreferences.contains(propertyChanged)) {
			BuilderUtil.buildWorkspace(IncrementalProjectBuilder.FULL_BUILD);
		}
		
	}
	
}
