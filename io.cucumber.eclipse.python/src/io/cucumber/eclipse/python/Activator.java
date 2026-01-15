package io.cucumber.eclipse.python;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import io.cucumber.eclipse.editor.validation.DocumentValidator;
import io.cucumber.eclipse.python.validation.BehaveGlueStore;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "io.cucumber.eclipse.python";

	// The shared instance
	private static Activator plugin;

	private IPropertyChangeListener propertyChangeListener;
	
	private ServiceTracker<BehaveGlueStore, BehaveGlueStore> glueStoreTracker;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		glueStoreTracker = new ServiceTracker<>(context, BehaveGlueStore.class, null);
		glueStoreTracker.open();
		propertyChangeListener = event -> DocumentValidator.revalidateAllDocuments();
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (glueStoreTracker != null) {
			glueStoreTracker.close();
			glueStoreTracker = null;
		}
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

	/**
	 * Returns the BehaveGlueStore service, or null if not available
	 */
	public static BehaveGlueStore getBehaveGlueStore() {
		Activator activator = getDefault();
		if (activator == null || activator.glueStoreTracker == null) {
			return null;
		}
		return activator.glueStoreTracker.getService();
	}

}
