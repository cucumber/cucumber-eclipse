package cucumber.eclipse.editor.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import cucumber.eclipse.editor.Activator;

public class ExtensionRegistryUtil {

	final static String EXTENSION_POINT_STEPDEFINITIONS_ID = "cucumber.eclipse.steps.integration";
	
	public static <T> List<T> getIntegrationExtensionsOfType(Class<T> clazz) {
		List<T> extensions = new ArrayList<T>();
		
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_STEPDEFINITIONS_ID);
		
		for (IConfigurationElement ce : config) {
			try {
				Object obj = ce.createExecutableExtension("class");
				
				if (clazz.isAssignableFrom(obj.getClass())) {
					@SuppressWarnings("unchecked")
					T value = (T) obj;
					
					extensions.add(value);
				}
			} catch (Exception e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Couldn't instantiate extension", e));
			}
		}
		
		return extensions;
	}
}
