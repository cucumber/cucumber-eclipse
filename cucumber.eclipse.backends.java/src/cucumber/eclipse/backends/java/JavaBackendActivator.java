package cucumber.eclipse.backends.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JavaBackendActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = JavaBackendActivator.class.getPackage().getName();

	private static JavaBackendActivator plugin;

	private IWorkspace workspace;
	private static List<IResourceChangeListener> listeners = new ArrayList<>();

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		workspace = ResourcesPlugin.getWorkspace();
	}

	public void stop(BundleContext context) throws Exception {
		if (workspace != null) {
			for (IResourceChangeListener listener : listeners) {
				workspace.removeResourceChangeListener(listener);
			}
			workspace = null;
		}
		plugin = null;
		super.stop(context);

	}

	public static JavaBackendActivator getDefault() {
		return plugin;
	}

	public static void addResourceChangeListener(IResourceChangeListener listener) {
		JavaBackendActivator activator = getDefault();
		if (activator.workspace != null) {
			activator.workspace.addResourceChangeListener(listener);
			listeners.add(listener);
		}
	}

}
