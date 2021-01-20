package io.cucumber.eclipse.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;

public class ExtensionRegistryUtil {

	private final static String EXTENSION_POINT_STEPDEFINITIONS_ID = "io.cucumber.eclipse.editor.steps.integration";
//	final static String EXTENSION_POINT_STEPDEFINITIONS_OPENER = "cucumber.eclipse.editor.step_definition_opener";
	
	private static <T> List<T> getIntegrationExtensionsOfType(String extensionPointId, Class<T> clazz) {
		List<T> extensions = new ArrayList<T>();
		
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(extensionPointId);
		
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
	
	
	public static List<IStepDefinitionsProvider> getStepDefinitionsProvider() {
		return getIntegrationExtensionsOfType(EXTENSION_POINT_STEPDEFINITIONS_ID, IStepDefinitionsProvider.class);
	}
//	
//	public static List<IStepDefinitionGenerator> getStepDefinitionGenerator() {
//		return getIntegrationExtensionsOfType(EXTENSION_POINT_STEPDEFINITIONS_ID, IStepDefinitionGenerator.class);
//	}
//	
//	public static List<IStepDefinitionOpener> getStepDefinitionOpener() {
//		return getIntegrationExtensionsOfType(EXTENSION_POINT_STEPDEFINITIONS_OPENER, IStepDefinitionOpener.class);
//	}
}
