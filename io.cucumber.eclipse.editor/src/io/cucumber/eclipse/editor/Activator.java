package io.cucumber.eclipse.editor;

import java.util.Collections;
import java.util.Hashtable;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String ICON_CUKES = "icons/cukes.gif";

	// The plug-in ID
	public static final String PLUGIN_ID = "io.cucumber.eclipse.editor"; //$NON-NLS-1$

	public static final String DEBUG_MODEL_ID = "gherking.debugModel";

	// The shared instance
	private static Activator plugin;
	
	private static final Tracing TRACING = new Tracing();

	private ServiceRegistration<DebugOptionsListener> tracingRegistration;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		tracingRegistration = context.registerService(DebugOptionsListener.class, TRACING,
				new Hashtable<>(Collections.singletonMap(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID)));
		// trigger activation of the service registry
		new ServiceTracker<>(context, CucumberServiceRegistry.class, null).open();
	}


	@Override
	public void stop(BundleContext context) throws Exception {
		tracingRegistration.unregister();
		TRACING.optionsChanged(null);
		plugin = null;
		super.stop(context);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(ICON_CUKES, imageDescriptorFromPlugin(PLUGIN_ID, ICON_CUKES));
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
