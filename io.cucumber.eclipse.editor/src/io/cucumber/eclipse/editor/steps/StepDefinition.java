package io.cucumber.eclipse.editor.steps;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.core.resources.IResource;

import io.cucumber.eclipse.editor.Tracing;

/**
 * A parse stepdefinition that relates either to a source file or a classpath
 * item
 *
 * @author Christoph Läubrich
 *
 */
public final class StepDefinition {

	public static final int NO_LINE_NUMBER = -1;
	public static final String NO_SOURCE_NAME = null;
	public static final String NO_PACKAGE_NAME = null;
	public static final String NO_LABEL = null;
	public static final IResource NO_SOURCE = null;
	public static final Comparator<? super StepDefinition> EXPRESSION_TEXT_ORDER = (s1, s2) -> s1.getExpression().getText()
			.compareToIgnoreCase(s2.getExpression().getText());

	/**
	 * The location-derived attributes of a {@link StepDefinition} - grouped together since they're
	 * all resolved from the same underlying source lookup, not independently.
	 */
	public static final class ResolvedLocation {

		public static final ResolvedLocation NONE = new ResolvedLocation(NO_SOURCE, NO_LINE_NUMBER, NO_SOURCE_NAME,
				NO_PACKAGE_NAME);

		private final IResource source;
		private final int lineNumber;
		private final String sourceName;
		private final String packageName;

		public ResolvedLocation(IResource source, int lineNumber, String sourceName, String packageName) {
			this.source = source;
			this.lineNumber = lineNumber;
			this.sourceName = sourceName;
			this.packageName = packageName;
		}

		public IResource getSource() {
			return source;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public String getSourceName() {
			return sourceName;
		}

		public String getPackageName() {
			return packageName;
		}

	}

	private final ExpressionDefinition expression;
	private final String label;
	private final String id;

	private volatile Supplier<ResolvedLocation> locationSupplier;
	private volatile ResolvedLocation location;
	private volatile boolean locationResolved;

	private volatile Supplier<StepParameter[]> parametersSupplier;
	private volatile StepParameter[] parameters;
	private volatile boolean parametersResolved;

	private volatile Supplier<String> descriptionSupplier;
	private volatile String description;
	private volatile boolean descriptionResolved;

	/**
	 * Creates a new {@link StepDefinition}
	 *
	 * @param id                 the persistent id of this step, this might be used by plugins to
	 *                           uniquely identify a step across others in the workspace
	 * @param label              a userfriendly label
	 * @param expression         the expresion that this step contains
	 * @param locationSupplier   resolves this step's source/line-number/source-name/package-name,
	 *                           lazily on the first actual call to any of {@link #getSource()},
	 *                           {@link #getLineNumber()}, {@link #getSourceName()},
	 *                           {@link #getPackageName()}, and memoized from then on - useful when
	 *                           computing this upfront (e.g. resolving a JDT method) is expensive and
	 *                           most step definitions never have their location looked at
	 * @param parametersSupplier resolves the parameters of the corresponding method, lazily on the
	 *                           first actual call to {@link #getParameters()} and memoized from then
	 *                           on - useful when resolving parameter types (e.g. enum constant
	 *                           values) upfront is expensive and most step definitions never have
	 *                           their parameters looked at (only actually needed when a proposal for
	 *                           this step is inserted)
	 */
	public StepDefinition(String id, String label, ExpressionDefinition expression,
			Supplier<ResolvedLocation> locationSupplier, Supplier<StepParameter[]> parametersSupplier,
			String description) {
		this(id, label, expression, locationSupplier, parametersSupplier, () -> description);
	}

	/**
	 * Same as {@link #StepDefinition(String, String, ExpressionDefinition, Supplier, Supplier,
	 * String)} but the description is resolved lazily too, on the first actual call to
	 * {@link #getDescription()}, and memoized from then on - useful when computing the description
	 * upfront (e.g. rendering Javadoc) is expensive and most step definitions never have their
	 * description looked at.
	 */
	public StepDefinition(String id, String label, ExpressionDefinition expression,
			Supplier<ResolvedLocation> locationSupplier, Supplier<StepParameter[]> parametersSupplier,
			Supplier<String> descriptionSupplier) {
		this.id = id;
		this.label = label;
		this.expression = expression;
		this.locationSupplier = locationSupplier;
		this.parametersSupplier = parametersSupplier;
		this.descriptionSupplier = descriptionSupplier;
	}

	private ResolvedLocation resolveLocation() {
		if (!locationResolved) {
			synchronized (this) {
				if (!locationResolved) {
					Supplier<ResolvedLocation> supplier = locationSupplier;
					location = Objects.requireNonNullElse(supplier == null ? null : supplier.get(),
							ResolvedLocation.NONE);
					locationSupplier = null;
					locationResolved = true;
				}
			}
		}
		return location;
	}

	public StepParameter[] getParameters() {
		if (!parametersResolved) {
			synchronized (this) {
				if (!parametersResolved) {
					long start = Tracing.PERF_STEPS ? System.nanoTime() : 0;
					StepParameter[] resolved = parametersSupplier == null ? null : parametersSupplier.get();
					parameters = Objects.requireNonNullElseGet(resolved, () -> new StepParameter[0]);
					if (Tracing.PERF_STEPS) {
						Tracing.get().trace(Tracing.PERFORMANCE_STEPS, "StepDefinition.getParameters(): lazily "
								+ "computed " + parameters.length + " parameter(s) for '" + id + "' in "
								+ ((System.nanoTime() - start) / 1_000_000) + "ms");
					}
					parametersSupplier = null;
					parametersResolved = true;
				}
			}
		}
		return parameters;
	}

	public IResource getSource() {
		return resolveLocation().getSource();
	}

	public int getLineNumber() {
		return resolveLocation().getLineNumber();
	}

	public String getSourceName() {
		ResolvedLocation resolved = resolveLocation();
		String sourceName = resolved.getSourceName();
		if (sourceName == null && resolved.getSource() != null) {
			return resolved.getSource().getName();
		}
		return sourceName;
	}

	public String getPackageName() {
		return resolveLocation().getPackageName();
	}

	public String getDescription() {
		if (!descriptionResolved) {
			synchronized (this) {
				if (!descriptionResolved) {
					long start = Tracing.PERF_STEPS ? System.nanoTime() : 0;
					description = descriptionSupplier == null ? null : descriptionSupplier.get();
					if (Tracing.PERF_STEPS) {
						Tracing.get().trace(Tracing.PERFORMANCE_STEPS, "StepDefinition.getDescription(): lazily "
								+ "computed description for '" + id + "' in " + ((System.nanoTime() - start) / 1_000_000)
								+ "ms");
					}
					descriptionSupplier = null;
					descriptionResolved = true;
				}
			}
		}
		return description;
	}

	/**
	 *
	 * @return the id to identify this step in a persitent manner
	 */
	public String getId() {
		return id;
	}

	public String getLabel() {
		if (label == null) {
			return getSourceName() + ":" + getLineNumber();
		}
		return label;
	}

	@Override
	public String toString() {
		IResource source = getSource();
		int lineNumber = getLineNumber();

		// For Steps from Current-Project
		if (lineNumber != 0)
			return "Step [text=" + getExpression() + ", source=" + source + ", lineNumber=" + lineNumber + "]";

		// For Steps From External-ClassPath JAR
		else
			return "Step [text=" + getExpression() + ", source=" + getSourceName() + ", package=" + getPackageName()
					+ "]";
	}

	public ExpressionDefinition getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StepDefinition other = (StepDefinition) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

}
