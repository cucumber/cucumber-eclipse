package io.cucumber.eclipse.java.cache;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

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

}
