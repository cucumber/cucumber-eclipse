package cucumber.eclipse.steps.integration;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IResource;

import io.cucumber.cucumberexpressions.Argument;
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

	private final IResource source;
	private final int lineNumber;
	private final Expression expression;
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
	public StepDefinition(String id, String label, Expression expression, IResource source, int lineNumber,
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
		List<Argument<?>> match = this.match(stepDefinitionText);
		return match != null;
	}

	public List<Argument<?>> match(String s) {
		return this.expression.match(s);
	}

	public String getSourceName() {
		if (sourceName == null && source !=null) {
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
			return "Step [text=" + getExpression().getSource() + ", source=" + source + ", lineNumber=" + lineNumber
					+ "]";

		// For Steps From External-ClassPath JAR
		else
			return "Step [text=" + getExpression().getSource() + ", source=" + sourceName + ", package=" + packageName
					+ "]";
	}

	public Expression getExpression() {
		return expression;
	}

	/**
	 * creates an expression out of lang and text
	 * 
	 * @param lang
	 * @param text
	 * @return
	 */
	public static Expression parseText(String lang, String text) {
		Locale locale = lang == null ? Locale.getDefault() : new Locale(lang);
		return new ExpressionFactory(new ParameterTypeRegistry(locale)).createExpression(text);
	}

}
