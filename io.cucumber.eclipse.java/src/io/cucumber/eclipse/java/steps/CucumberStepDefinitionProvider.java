package io.cucumber.eclipse.java.steps;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextViewer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.cucumber.eclipse.editor.steps.ExpressionDefinition;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinition.ResolvedLocation;
import io.cucumber.eclipse.editor.steps.StepParameter;
import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.cache.JavaGlueModelCache;
import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.validation.JavaGlueStore;

/**
 * Step definition provider that calls cucumber to find steps for the project
 *
 * @author christoph
 *
 */
@Component(service = IStepDefinitionsProvider.class, property = {
		IStepDefinitionsProvider.PROVIDER_NAME + "=Cucumber JVM Runtime", Constants.SERVICE_RANKING + ":Integer=100" })
public class CucumberStepDefinitionProvider extends JavaStepDefinitionsProvider {

	private final JavaGlueStore javaValidator;
	private final JavaGlueModelCache modelCache;

	@Activate
	public CucumberStepDefinitionProvider(@Reference JavaGlueStore validator, @Reference JavaGlueModelCache modelCache)
			throws URISyntaxException {
		javaValidator = validator;
		this.modelCache = modelCache;
	}

	@Override
	public Collection<StepDefinition> findStepDefinitions(ITextViewer viewer, int offset, IResource resource,
			IProgressMonitor monitor) throws CoreException {
		try {
			IJavaProject javaProject = JDTUtil.getJavaProject(resource);
			Collection<CucumberStepDefinition> steps = javaValidator.getAvailableSteps(viewer.getDocument());

			boolean perf = Tracing.PERF_STEPS;
			long start = perf ? System.currentTimeMillis() : 0;
			if (perf) {
				Tracing.get().trace(Tracing.PERFORMANCE_STEPS,
						"findStepDefinitions: building " + steps.size() + " step definition(s) for "
								+ resource.getName() + " - JDT resolution is deferred until a proposal's"
								+ " description/parameters/location is actually requested; see"
								+ " LazyStepMethod's own trace line for if/when that happens");
			}

			// Shared per-invocation cache so multiple steps declared in the same class only resolve
			// that class's IType once, if/when their lazy resolution actually runs.
			Map<String, IType> typeBuffer = new ConcurrentHashMap<>();
			Collection<StepDefinition> result = steps.parallelStream()
					.map(cucumberStep -> parseStepDefintion(cucumberStep, javaProject, typeBuffer))
					.collect(Collectors.toList());

			if (perf) {
				Tracing.get().trace(Tracing.PERFORMANCE_STEPS, "findStepDefinitions: built "
						+ result.size() + "/" + steps.size() + " in " + (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (OperationCanceledException e) {
		}
		return Collections.emptyList();
	}

	private StepDefinition parseStepDefintion(CucumberStepDefinition cucumberStep, IJavaProject project,
			Map<String, IType> typeBuffer) {
		CucumberCodeLocation codeLocation = cucumberStep.getCodeLocation();
		io.cucumber.plugin.event.StepDefinition cucumberStepDefinition = cucumberStep.getStepDefinition();
		String location = cucumberStepDefinition.getLocation();
		ExpressionDefinition expression = new ExpressionDefinition(cucumberStepDefinition.getPattern());
		LazyStepMethod lazyMethod = new LazyStepMethod(modelCache, project, typeBuffer, codeLocation);
		return new StepDefinition(location, buildLabel(codeLocation), expression,
				() -> resolveLocation(lazyMethod.resolve()), () -> resolveParameters(lazyMethod.resolve()),
				() -> resolveJavadoc(lazyMethod.resolve()));
	}

	/**
	 * Builds a step's label directly from Cucumber's own reported code location - the same
	 * information {@link JDTUtil#getMethodName(IMethod)} would produce from a resolved method, but
	 * without needing to resolve anything via JDT first.
	 */
	private static String buildLabel(CucumberCodeLocation codeLocation) {
		String typeName = codeLocation.getTypeName();
		int lastDot = typeName.lastIndexOf('.');
		String simpleName = lastDot >= 0 ? typeName.substring(lastDot + 1) : typeName;
		return simpleName + "." + codeLocation.getMethodName() + "(" + String.join(",", codeLocation.getParameter())
				+ ")";
	}

	private ResolvedLocation resolveLocation(IMethod method) {
		if (method == null) {
			return ResolvedLocation.NONE;
		}
		try {
			IType type = method.getDeclaringType();
			int lineNumber = modelCache.getLineNumber(method.getCompilationUnit(), method);
			return new ResolvedLocation(type.getResource(), lineNumber, method.getElementName(),
					type.getPackageFragment().getElementName());
		} catch (JavaModelException e) {
			return ResolvedLocation.NONE;
		}
	}

	private StepParameter[] resolveParameters(IMethod method) {
		if (method == null) {
			return new StepParameter[0];
		}
		try {
			return getParameter(method);
		} catch (JavaModelException e) {
			return new StepParameter[0];
		}
	}

	private String resolveJavadoc(IMethod method) {
		return method == null ? null : modelCache.getJavadoc(method);
	}

	/**
	 * Lazily resolves the {@link IMethod} backing a step definition - the actual expensive JDT work
	 * (find the declaring type, fetch its methods, disambiguate overloads) is deferred until
	 * {@link #resolve()} is first called, and memoized from then on, so it happens at most once per
	 * step regardless of how many of {@link StepDefinition}'s lazy accessors end up needing it.
	 */
	private static final class LazyStepMethod {

		private final JavaGlueModelCache modelCache;
		private final IJavaProject project;
		private final Map<String, IType> typeBuffer;
		private final CucumberCodeLocation codeLocation;

		private volatile IMethod method;
		private volatile boolean resolved;

		LazyStepMethod(JavaGlueModelCache modelCache, IJavaProject project, Map<String, IType> typeBuffer,
				CucumberCodeLocation codeLocation) {
			this.modelCache = modelCache;
			this.project = project;
			this.typeBuffer = typeBuffer;
			this.codeLocation = codeLocation;
		}

		IMethod resolve() {
			if (!resolved) {
				synchronized (this) {
					if (!resolved) {
						long start = Tracing.PERF_STEPS ? System.nanoTime() : 0;
						try {
							method = doResolve();
						} catch (RuntimeException e) {
							// resolution can now run long after findStepDefinitions returned (e.g. on
							// template insertion or hover) - the project/type/method may no longer be
							// in the state it was when this step was first listed, so fail open rather
							// than propagate into StepDefinition's lazy accessors.
							method = null;
						}
						if (Tracing.PERF_STEPS) {
							Tracing.get().trace(Tracing.PERFORMANCE_STEPS,
									"LazyStepMethod.resolve(): resolved '" + codeLocation + "' to "
											+ (method != null ? method.getHandleIdentifier() : "<no match>") + " in "
											+ ((System.nanoTime() - start) / 1_000_000) + "ms");
						}
						resolved = true;
					}
				}
			}
			return method;
		}

		private IMethod doResolve() {
			IType type = typeBuffer.computeIfAbsent(codeLocation.getTypeName(), typeName -> {
				try {
					return project.findType(typeName, (IProgressMonitor) null);
				} catch (JavaModelException e) {
					return null;
				}
			});
			if (type == null) {
				return null;
			}
			try {
				IMethod[] typeMethods = modelCache.getMethods(type);
				IMethod[] methods = modelCache.resolveTypeMethod(typeMethods, codeLocation);
				if (methods != null && methods.length == 1) {
					return methods[0];
				}
			} catch (JavaModelException e) {
			}
			return null;
		}

	}

}
