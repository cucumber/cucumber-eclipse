package io.cucumber.eclipse.python;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import io.cucumber.eclipse.editor.validation.DocumentValidator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "io.cucumber.eclipse.python";

	// The shared instance
	private static Activator plugin;

	private IPropertyChangeListener propertyChangeListener;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		propertyChangeListener = event -> DocumentValidator.revalidateAllDocuments();
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (propertyChangeListener != null) {
			getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
