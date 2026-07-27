package io.cucumber.eclipse.java.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.Tracing;

@Component(service = JavaGlueModelCache.class)
public class JavaGlueModelCacheService implements JavaGlueModelCache {

	private final Map<IType, CachedMethods> cache = new ConcurrentHashMap<>();

	private static final class CachedMethods {
		final long modStamp;
		final IMethod[] methods;

		CachedMethods(long modStamp, IMethod[] methods) {
			this.modStamp = modStamp;
			this.methods = methods;
		}
	}

	@Override
	public IMethod[] getMethods(IType type) {
		IResource resource = type.getResource();
		long modStamp = resource != null ? resource.getModificationStamp() : IResource.NULL_STAMP;
		return cache.compute(type, (t, existing) -> {
			if (existing != null && existing.modStamp == modStamp) {
				return existing;
			}
			long start = Tracing.PERF_STEPS ? System.nanoTime() : 0;
			IMethod[] methods;
			try {
				methods = t.getMethods();
			} catch (JavaModelException e) {
				methods = new IMethod[0];
			}
			if (Tracing.PERF_STEPS) {
				Tracing.get().trace(Tracing.PERFORMANCE_STEPS, "JavaGlueModelCache: MISS for '" + t.getElementName()
						+ "' - fetched " + methods.length + " method(s) in " + ((System.nanoTime() - start) / 1_000_000)
						+ "ms (modStamp=" + modStamp + ")");
			}
			return new CachedMethods(modStamp, methods);
		}).methods;
	}

}
