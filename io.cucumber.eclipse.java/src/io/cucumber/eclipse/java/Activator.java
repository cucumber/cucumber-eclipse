package io.cucumber.eclipse.java;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import io.cucumber.eclipse.editor.EnvelopeReader;
import io.cucumber.eclipse.editor.validation.DocumentValidator;
import io.cucumber.eclipse.java.cache.JavaGlueModelCache;
import io.cucumber.eclipse.java.validation.JavaGlueStore;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "io.cucumber.eclipse.java"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ServiceTracker<EnvelopeReader, EnvelopeReader> envelopeReaderTracker;
	
	private IPropertyChangeListener propertyChangeListener;

	private ServiceTracker<JavaGlueStore, JavaGlueStore> glueStoreTracker;

	private ServiceTracker<JavaGlueModelCache, JavaGlueModelCache> modelCacheTracker;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		envelopeReaderTracker = new ServiceTracker<>(context, EnvelopeReader.class, null);
		envelopeReaderTracker.open();
		glueStoreTracker = new ServiceTracker<>(context, JavaGlueStore.class, null);
		glueStoreTracker.open();
		modelCacheTracker = new ServiceTracker<>(context, JavaGlueModelCache.class, null);
		modelCacheTracker.open();
		propertyChangeListener = event -> DocumentValidator.revalidateAllDocuments();
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (envelopeReaderTracker != null) {
			envelopeReaderTracker.close();
			envelopeReaderTracker = null;
		}
		if (glueStoreTracker != null) {
			glueStoreTracker.close();
			glueStoreTracker = null;
		}
		if (modelCacheTracker != null) {
			modelCacheTracker.close();
			modelCacheTracker = null;
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
	 * Returns the EnvelopeReader service, or null if not available
	 */
	public static EnvelopeReader getEnvelopeReader() {
		Activator activator = getDefault();
		if (activator == null || activator.envelopeReaderTracker == null) {
			return null;
		}
		return activator.envelopeReaderTracker.getService();
	}

	/**
	 * Returns the EnvelopeReader service, or null if not available
	 */
	public static JavaGlueStore getJavaGlueStore() {
		Activator activator = getDefault();
		if (activator == null || activator.glueStoreTracker == null) {
			return null;
		}
		return activator.glueStoreTracker.getService();
	}

	/**
	 * Returns the JavaGlueModelCache service, or null if not available
	 */
	public static JavaGlueModelCache getJavaGlueModelCache() {
		Activator activator = getDefault();
		if (activator == null || activator.modelCacheTracker == null) {
			return null;
		}
		return activator.modelCacheTracker.getService();
	}

}
