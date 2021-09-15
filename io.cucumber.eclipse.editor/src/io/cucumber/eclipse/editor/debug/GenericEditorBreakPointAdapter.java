package io.cucumber.eclipse.editor.debug;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Workaround for Bug 575970
 * 
 * @author christoph
 *
 */
@Component(service = IAdapterFactory.class, property = {
		IAdapterFactory.SERVICE_PROPERTY_ADAPTABLE_CLASS
				+ "=org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor",
		IAdapterFactory.SERVICE_PROPERTY_ADAPTER_NAMES + "=org.eclipse.debug.ui.actions.IToggleBreakpointsTarget" })
public class GenericEditorBreakPointAdapter implements IAdapterFactory, IToggleBreakpointsTargetExtension2 {

	private volatile BundleContext bundleContext;
	private Map<ServiceReference<?>, LazyServiceReference> serviceMap = new ConcurrentHashMap<>();

	@Activate
	void start(BundleContext bc) {
		this.bundleContext = bc;
	}

	@Deactivate
	void stop() {
		this.bundleContext = null;
		serviceMap.values().forEach(LazyServiceReference::dispose);
	}

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE)
	void addService(ServiceReference<IToggleBreakpointsTarget> reference) {
		serviceMap.put(reference, new LazyServiceReference(() -> bundleContext, reference));
	}

	void removeService(ServiceReference<IToggleBreakpointsTarget> reference) {
		LazyServiceReference remove = serviceMap.remove(reference);
		if (remove != null) {
			remove.dispose();
		}
	}

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IToggleBreakpointsTarget.class || adapterType == IToggleBreakpointsTargetExtension.class
				|| adapterType == IToggleBreakpointsTargetExtension2.class) {
			return adapterType.cast(this);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IToggleBreakpointsTarget.class, IToggleBreakpointsTargetExtension.class,
				IToggleBreakpointsTargetExtension2.class };
	}

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		for (IToggleBreakpointsTarget e : targets(IToggleBreakpointsTarget.class)
				.filter(e -> e.canToggleLineBreakpoints(part, selection)).toArray(IToggleBreakpointsTarget[]::new)) {
			e.toggleLineBreakpoints(part, selection);
		}
	}

	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return targets(IToggleBreakpointsTarget.class).anyMatch(e -> e.canToggleLineBreakpoints(part, selection));
	}

	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		for (IToggleBreakpointsTarget e : targets(IToggleBreakpointsTarget.class)
				.filter(e -> e.canToggleMethodBreakpoints(part, selection)).toArray(IToggleBreakpointsTarget[]::new)) {
			e.toggleMethodBreakpoints(part, selection);
		}
	}

	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return targets(IToggleBreakpointsTarget.class).anyMatch(e -> e.canToggleMethodBreakpoints(part, selection));
	}

	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		for (IToggleBreakpointsTarget e : targets(IToggleBreakpointsTarget.class)
				.filter(e -> e.canToggleWatchpoints(part, selection)).toArray(IToggleBreakpointsTarget[]::new)) {
			e.toggleWatchpoints(part, selection);
		}
	}

	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return targets(IToggleBreakpointsTarget.class).anyMatch(e -> e.canToggleWatchpoints(part, selection));
	}

	// IToggleBreakpointsTargetExtension
	@Override
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		for (IToggleBreakpointsTargetExtension e : targets(IToggleBreakpointsTargetExtension.class)
				.filter(e -> e.canToggleBreakpoints(part, selection))
				.toArray(IToggleBreakpointsTargetExtension[]::new)) {
			e.toggleBreakpoints(part, selection);
		}
	}

	// IToggleBreakpointsTargetExtension
	@Override
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return targets(IToggleBreakpointsTargetExtension.class).anyMatch(e -> e.canToggleBreakpoints(part, selection));
	}

	// IToggleBreakpointsTargetExtension2
	@Override
	public void toggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event)
			throws CoreException {
		for (IToggleBreakpointsTargetExtension2 e : targets(IToggleBreakpointsTargetExtension2.class)
				.filter(e -> e.canToggleBreakpointsWithEvent(part, selection, event))
				.toArray(IToggleBreakpointsTargetExtension2[]::new)) {
			e.toggleBreakpointsWithEvent(part, selection, event);
		}
	}

	// IToggleBreakpointsTargetExtension2
	@Override
	public boolean canToggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) {
		return targets(IToggleBreakpointsTargetExtension2.class)
				.anyMatch(e -> e.canToggleBreakpointsWithEvent(part, selection, event));
	}

	private <T extends IToggleBreakpointsTarget> Stream<T> targets(Class<T> extension) {
		return serviceMap.values().stream()
				.sorted(Comparator.comparingInt(LazyServiceReference::getOrder).reversed()
						.thenComparingLong(LazyServiceReference::getServiceId))
				.map(LazyServiceReference::get).filter(Objects::nonNull).filter(extension::isInstance)
				.map(extension::cast);
	}

	private static final class LazyServiceReference implements Supplier<IToggleBreakpointsTarget> {
		private Supplier<BundleContext> contextSupplier;
		private ServiceReference<IToggleBreakpointsTarget> reference;
		private IToggleBreakpointsTarget target;
		private BundleContext bundleContext;
		private boolean disposed;

		LazyServiceReference(Supplier<BundleContext> contextSupplier,
				ServiceReference<IToggleBreakpointsTarget> reference) {
			this.contextSupplier = contextSupplier;
			this.reference = reference;
		}
		@Override
		public synchronized IToggleBreakpointsTarget get() {
			if (target == null && !disposed) {
				bundleContext = contextSupplier.get();
				if (bundleContext != null) {
					target = bundleContext.getService(reference);
				}
			}
			return target;
		}

		synchronized void dispose() {
			disposed = true;
			if (target != null) {
				bundleContext.ungetService(reference);
			}
		}

		int getOrder() {
			Object ranking = reference.getProperty(Constants.SERVICE_RANKING);
			if (ranking instanceof Number) {
				return ((Number) ranking).intValue();
			}
			return 0;
		}

		long getServiceId() {
			Object id = reference.getProperty(Constants.SERVICE_ID);
			if (id instanceof Number) {
				return ((Number) id).longValue();
			}
			return 0;
		}
	}

}
