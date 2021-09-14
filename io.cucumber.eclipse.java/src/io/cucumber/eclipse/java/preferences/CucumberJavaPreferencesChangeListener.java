package io.cucumber.eclipse.java.preferences;

import static io.cucumber.eclipse.java.preferences.CucumberJavaPreferences.PREF_ACTIVE_FILTERS_LIST;
import static io.cucumber.eclipse.java.preferences.CucumberJavaPreferences.PREF_INACTIVE_FILTERS_LIST;
import static io.cucumber.eclipse.java.preferences.CucumberJavaPreferences.PREF_USE_STEP_DEFINITIONS_FILTERS;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


public class CucumberJavaPreferencesChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyChanged = event.getProperty();

		List<String> listenedPreferences = Arrays.asList(PREF_USE_STEP_DEFINITIONS_FILTERS, PREF_ACTIVE_FILTERS_LIST, PREF_INACTIVE_FILTERS_LIST);
		
		if(listenedPreferences.contains(propertyChanged)) {
//			BuilderUtil.buildWorkspace(IncrementalProjectBuilder.FULL_BUILD);
		}
		
	}
	
}
