package io.cucumber.eclipse.java.cache;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;

@Component(service = JavaGlueModelCache.class)
public class JavaGlueModelCacheService implements JavaGlueModelCache {

	private final Map<IType, CachedMethods> cache = new ConcurrentHashMap<>();
	private final Map<IMethod, String[]> parameterNamesCache = new ConcurrentHashMap<>();

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

	@Override
	public String[] getParameterNames(IMethod method) throws JavaModelException {
		String[] cached = parameterNamesCache.get(method);
		if (cached != null) {
			return cached;
		}
		long start = Tracing.PERF_STEPS ? System.nanoTime() : 0;
		String[] resolved = JDTUtil.resolveMethodParameterNames(method);
		parameterNamesCache.put(method, resolved);
		if (Tracing.PERF_STEPS) {
			Tracing.get().trace(Tracing.PERFORMANCE_STEPS, "JavaGlueModelCache: cached parameter name(s) for '"
					+ method.getElementName() + "' in " + ((System.nanoTime() - start) / 1_000_000) + "ms");
		}
		return resolved;
	}

	@Override
	public IMethod[] resolveTypeMethod(IMethod[] typeMethods, CucumberCodeLocation codeLocation)
			throws JavaModelException {
		String methodName = codeLocation.getMethodName();
		if (methodName.isBlank()) {
			return null;
		}
		IMethod[] candidates = Arrays.stream(typeMethods)
				.filter(method -> method.getElementName().equals(methodName)).toArray(IMethod[]::new);
		if (candidates.length > 1) {
			long start = Tracing.PERF_STEPS ? System.nanoTime() : 0;
			String[] parameter = codeLocation.getParameter();
			IMethod[] disambiguated = Arrays.stream(candidates).filter(method -> {
				return method.getNumberOfParameters() == parameter.length;
			}).filter(method -> {
				try {
					String[] resolvedMethodParameterNames = getParameterNames(method);
					for (int i = 0; i < parameter.length; i++) {
						if (!resolvedMethodParameterNames[i].equals(parameter[i])) {
							return false;
						}
					}
					return true;
				} catch (JavaModelException e) {
					return false;
				}
			}).toArray(IMethod[]::new);
			if (Tracing.PERF_STEPS) {
				Tracing.get().trace(Tracing.PERFORMANCE_STEPS,
						"resolveTypeMethod: disambiguated " + candidates.length + " overload(s) of '" + methodName
								+ "' to " + disambiguated.length + " in " + ((System.nanoTime() - start) / 1_000_000)
								+ "ms");
			}
			return disambiguated;
		}
		return candidates;
	}

	@Override
	public IMethod[] resolveMethod(IJavaProject project, CucumberCodeLocation codeLocation, IProgressMonitor monitor)
			throws JavaModelException {
		String typeName = codeLocation.getTypeName();
		if (typeName.isBlank()) {
			return new IMethod[0];
		}
		IType type = project.findType(typeName, monitor);
		if (type == null) {
			return new IMethod[0];
		}
		return resolveTypeMethod(getMethods(type), codeLocation);
	}

}
