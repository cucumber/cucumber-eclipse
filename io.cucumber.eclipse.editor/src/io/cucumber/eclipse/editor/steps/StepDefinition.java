package io.cucumber.eclipse.editor.steps;

import java.util.Objects;

import org.eclipse.core.resources.IResource;

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

	private final IResource source;
	private final int lineNumber;
	private final ExpressionDefinition expression;
	private final String label;

	private final String sourceName;
	private final String packageName;
	private final String id;
	private StepParameter[] parameters;

	/**
	 * Creates a new {@link StepDefinition}
	 * 
	 * @param id             the persistent id of this step, this might be used by
	 *                       plugins to uniquely identify a step across others in
	 *                       the workspace
	 * @param label          a userfriendly label
	 * @param expression     the expresion that this step contains
	 * @param source         the source where this step is created from
	 * @param lineNumber     an optional line limber where in the resource the step
	 *                       was found use {@link #NO_LINE_NUMBER} in case where no
	 *                       is available
	 * @param sourceName     the name of the source, if not given, the name of te
	 *                       resource might be used
	 * @param packageName    the packagename of the source
	 * @param parameterNames the parameter names of the corresponding method
	 */
	public StepDefinition(String id, String label, ExpressionDefinition expression, IResource source, int lineNumber,
			String sourceName, String packageName, StepParameter[] parameters) {
		this.id = id;
		this.label = label;
		this.expression = expression;
		this.source = source;
		this.lineNumber = lineNumber;
		this.sourceName = sourceName;
		this.packageName = packageName;
		this.parameters = parameters;
	}

	public StepParameter[] getParameters() {
		return parameters;
	}

	public IResource getSource() {
		return source;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getSourceName() {
		if (sourceName == null && source != null) {
			return source.getName();
		}
		return sourceName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		String sourceLocation = Objects.requireNonNullElse(getSourceName(), "");
		if (sourceLocation != null) {
			if (sourceLocation.contains("/")) {
				sourceLocation = sourceLocation.substring(sourceLocation.lastIndexOf("/"));
			}
			sb.append(sourceLocation);
		}
		if (getLineNumber() > 0) {
			sb.append(":" + getLineNumber());
		}
		return sb.toString();
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
			return getSourceName() + ":" + this.lineNumber;
		}
		return label;
	}

	@Override
	public String toString() {

		// For Steps from Current-Project
		if (lineNumber != 0)
			return "Step [text=" + getExpression() + ", source=" + source + ", lineNumber=" + lineNumber + "]";

		// For Steps From External-ClassPath JAR
		else
			return "Step [text=" + getExpression() + ", source=" + sourceName + ", package=" + packageName + "]";
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
		result = prime * result + lineNumber;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
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
		if (lineNumber != other.lineNumber)
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (sourceName == null) {
			if (other.sourceName != null)
				return false;
		} else if (!sourceName.equals(other.sourceName))
			return false;
		return true;
	}

}
