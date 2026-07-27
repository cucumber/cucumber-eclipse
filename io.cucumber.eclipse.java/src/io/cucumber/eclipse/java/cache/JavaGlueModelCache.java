package io.cucumber.eclipse.java.cache;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;

/**
 * Caches results of JDT model queries used while resolving Cucumber glue code, so repeated
 * content-assist invocations don't repeat a query whose result hasn't changed. Implementations
 * invalidate an entry once the underlying source has been modified.
 */
public interface JavaGlueModelCache {

	/**
	 * @param type the type to get the declared methods for
	 * @return the same result as {@link IType#getMethods()}, cached until {@code type}'s
	 *         underlying source changes
	 */
	IMethod[] getMethods(IType type);

	/**
	 * @param method the method to resolve fully qualified parameter type names for
	 * @return the resolved parameter type names, cached for the lifetime of the {@link IMethod}
	 *         handle (a method whose parameter types change resolves to a different handle)
	 */
	String[] getParameterNames(IMethod method) throws JavaModelException;

	/**
	 * Same as resolving a {@code codeLocation} against {@code typeMethods} directly, but using
	 * {@link #getParameterNames(IMethod)} to disambiguate any overloaded candidates, instead of
	 * resolving their parameter names from scratch.
	 */
	IMethod[] resolveTypeMethod(IMethod[] typeMethods, CucumberCodeLocation codeLocation) throws JavaModelException;

	/**
	 * Same as {@link #resolveTypeMethod(IMethod[], CucumberCodeLocation)} but also resolves
	 * {@code codeLocation}'s declaring type through {@link #getMethods(IType)}, for callers that
	 * don't already have the type's methods in hand.
	 */
	IMethod[] resolveMethod(IJavaProject project, CucumberCodeLocation codeLocation, IProgressMonitor monitor)
			throws JavaModelException;

}
