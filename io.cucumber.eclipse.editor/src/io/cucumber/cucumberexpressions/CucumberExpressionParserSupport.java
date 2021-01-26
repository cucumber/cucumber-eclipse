package io.cucumber.cucumberexpressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateVariable;

import io.cucumber.cucumberexpressions.Ast.Node;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepParameter;

/**
 * This class exits to access the (non-public) cucumber-expressions API
 * 
 * @author christoph
 *
 */
//TODO see bug Bug 570519  currently we must always supply the full text as the default value
//TODO use public API (if available)
public class CucumberExpressionParserSupport {

	private static final Pattern BEGIN_ANCHOR = Pattern.compile("^\\^.*");
	private static final Pattern END_ANCHOR = Pattern.compile(".*\\$$");
	private static final Pattern SCRIPT_STYLE_REGEXP = Pattern.compile("^/(.*)/$");

	/**
	 * Creates a {@link Template} from {@link StepDefinition}
	 * 
	 * @param definition the step definition to use
	 * @param contextId  the context id that should be associated with the template
	 * @return a template representing this step definition
	 */
	public static Template createTemplate(StepDefinition definition, String contextId) {
		String expressionString = definition.getExpression().getText();
		if (BEGIN_ANCHOR.matcher(expressionString).find() || END_ANCHOR.matcher(expressionString).find()) {
			return new Template(definition.getExpression().getText(), definition.getLabel(), contextId,
					definition.getExpression().getText(), true);
		} else {
			Matcher m = SCRIPT_STYLE_REGEXP.matcher(expressionString);
			if (m.find()) {
				return new Template(definition.getExpression().getText(), definition.getLabel(), contextId,
						definition.getExpression().getText(), true);
			} else {
				return new CucumberExpressionTemplate(definition, contextId);
			}
		}
	}

	/**
	 * Evaluates the {@link Template} into a {@link TemplateBuffer}
	 * 
	 * @param template the template to evaluate
	 * @return the parsed {@link TemplateBuffer}
	 */
	public static TemplateBuffer evaluate(Template template) {
		String pattern = template.getPattern();
		if (template instanceof CucumberExpressionTemplate) {
			CucumberExpressionTemplate cucumberTemplate = (CucumberExpressionTemplate) template;
			CucumberExpressionParser parser = new CucumberExpressionParser();
			Node ast = parser.parse(pattern);
			AtomicInteger counter = new AtomicInteger();
			List<TemplateVariable> variables = new ArrayList<>();
			parseAst(ast, Arrays.stream(cucumberTemplate.definition.getParameters()).iterator(), counter, variables);
			TemplateBuffer buffer = new TemplateBuffer(pattern, variables.toArray(TemplateVariable[]::new));
			return buffer;
		} else {
			return new TemplateBuffer(pattern, new TemplateVariable[0]);
		}
	}

	private static void parseAst(Node node, Iterator<StepParameter> parameterNames, AtomicInteger counter,
			List<TemplateVariable> variables) {
		switch (node.type()) {
		case PARAMETER_NODE:
			variables.add(parseParameter(node, parameterNames, counter));
			break;
		case ALTERNATION_NODE:
			variables.add(parseAlternation(node));
			break;
		case OPTIONAL_NODE:
			variables.add(parseOptional(node));
			break;
		default:
			List<Node> childs = node.nodes();
			if (childs != null) {
				for (Node child : childs) {
					parseAst(child, parameterNames, counter, variables);
				}
			}
			break;
		}

	}

	private static TemplateVariable parseOptional(Node node) {
		Node child = node.nodes().get(0);
		String optionalText = child.text();
		String fullText = "(" + optionalText + ")";
		TemplateVariable variable = new TemplateVariable("OPTIONAL_NODE", fullText, fullText,
				new int[] { node.start() });
		variable.setValue(optionalText);
		return variable;
	}

	private static TemplateVariable parseAlternation(Node node) {
		List<String> values = new ArrayList<>();
		node.nodes().stream().map(Node::text).forEach(values::add);
		String join = String.join("/", values);
		values.add(0, join);
		TemplateVariable variable = new TemplateVariable("ALTERNATION_NODE", join, values.toArray(String[]::new),
				new int[] { node.start() });
		return variable;
	}

	private static TemplateVariable parseParameter(Node node, Iterator<StepParameter> parameterNames,
			AtomicInteger counter) {
		List<Node> nodes = node.nodes();
		String paramType;
		if (nodes.isEmpty()) {
			paramType = "";
		} else {
			paramType = nodes.get(0).text();
		}
		StepParameter param = parameterNames.hasNext() ? parameterNames.next() : null;
		int index = counter.getAndIncrement();
		String paramName;
		String[] paramValues;
		String fullText = "{" + paramType + "}";
		if (param == null) {
			paramName = paramType + index;
			paramValues = new String[] { paramName };
		} else {
			paramName = param.getParameterName();
			String[] values = param.getValues();
			if (values == null || values.length == 0) {
				paramValues = new String[] { paramName };
			} else {
				paramValues = values;
			}
		}
		TemplateVariable variable = new TemplateVariable(paramType, fullText, fullText, new int[] { node.start() });
		variable.setValues(paramValues);
		return variable;
	}

	private static final class CucumberExpressionTemplate extends Template {

		private StepDefinition definition;

		public CucumberExpressionTemplate(StepDefinition definition, String contextId) {
			super(definition.getExpression().getText(), definition.getLabel(), contextId,
					definition.getExpression().getText(), true);
			this.definition = definition;

		}

	}
}
