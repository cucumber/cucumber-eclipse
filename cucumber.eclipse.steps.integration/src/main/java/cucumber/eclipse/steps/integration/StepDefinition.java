package cucumber.eclipse.steps.integration;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IResource;

import cucumber.eclipse.steps.integration.marker.MarkerFactory;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

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
	private transient Expression cucumberExpression;
	private transient boolean expressionFailed;
	private final String label;

	private final String sourceName;
	private final String packageName;
	private final String id;

	/**
	 * Creates a new {@link StepDefinition}
	 * 
	 * @param id
	 *            the persitent id of this step, this might be used by plugins
	 *            to uniquily identify a step accros others in the workspace
	 * @param label
	 *            a userfriendly label
	 * @param expression
	 *            the expresion that this step contains
	 * @param source
	 *            the source where this step is created from
	 * @param lineNumber
	 *            an optional line limber where in the resource the step was
	 *            found use {@link #NO_LINE_NUMBER} in case where no is avaiable
	 * @param sourceName
	 *            the name of the source, if not given, the name of te resource
	 *            might be used
	 * @param packageName
	 *            the packagename of the source
	 */
	public StepDefinition(String id, String label, ExpressionDefinition expression, IResource source, int lineNumber,
			String sourceName, String packageName) {
		this.id = id;
		this.label = label;
		this.expression = expression;
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

	public boolean matches(String stepDefinitionText) {
		return this.match(stepDefinitionText) != null;

	}

	public List<Argument<?>> match(String s) {
		try {
			if (cucumberExpression == null) {
				if (expressionFailed) {
					return null;
				}
				cucumberExpression = parseText(expression.getLang(), expression.getText());
			}
			return cucumberExpression.match(s);
		} catch (CucumberExpressionException e) {
			expressionFailed = true;
			if (source != null) {
				MarkerFactory.INSTANCE.syntaxErrorOnStepDefinition(source, e, lineNumber);
			}
			return null;
		}

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

	/**
	 * creates an expression out of lang and text, this is an intermediate
	 * soloution since we better need to create the factory out of the java
	 * project where the gherking file resides
	 * 
	 * @param lang
	 * @param text
	 * @return
	 */
	private static Expression parseText(String lang, String text) {
		Locale locale = lang == null ? Locale.getDefault() : new Locale(lang);
		return new ExpressionFactory(new ParameterTypeRegistry(locale)).createExpression(text);
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
