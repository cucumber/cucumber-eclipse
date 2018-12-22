package cucumber.eclipse.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import cucumber.eclipse.editor.preferences.StepDefinitionsScanPropertyChangeListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "cucumber.eclipse.editor"; //$NON-NLS-1$

	// The BundleA
	private Bundle bundle = null;

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Instantiate Bundle
		bundle = context.getBundle();
		
		this.setupPreferenceChangesListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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

	@Override
	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();

	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	// return Bundle
	public Bundle get_Bundle() {
		return bundle;
	}

	private void setupPreferenceChangesListeners() {
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new StepDefinitionsScanPropertyChangeListener());
	}
	
}
