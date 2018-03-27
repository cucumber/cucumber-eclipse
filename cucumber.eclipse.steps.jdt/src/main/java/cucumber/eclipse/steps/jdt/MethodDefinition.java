package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;

/**
 * @author girija.panda@nokia.com 
 * Purpose : Used to get method details i.e.
 *         name/return-type/body from a source file Specially used to collect
 *         all steps from the java8-Lambda-Expressions used in
 *         methods/constructors of a class
 *
 */

public class MethodDefinition {

	private SimpleName methodName;
	private Type returnType;

	// cucumber.api.java8.<lang>(ex. cucumber.api.java8.En)
	public static final String REGEX_JAVA8_CUKEAPI = "cucumber\\.api\\.java8\\.(.*)";
	public static final String CONTAINS_JAVA8_CUKEAPI = "(.*)cucumber\\.api\\.java8\\.(.*)";

	private List<Statement> methodBodyList = new ArrayList<Statement>();
	private String lang;

	public String[] java8CukeImport = null;

	public MethodDefinition() {

	}

	/**
	 * @param methodName
	 * @param returnType
	 * @param methodBodyList
	 */
	public MethodDefinition(SimpleName methodName, Type returnType, List<Statement> methodBodyList) {
		super();
		this.methodName = methodName;
		this.returnType = returnType;
		this.methodBodyList = methodBodyList;
	}

	public void setJava8CukeLang(String importDeclaration) {
		this.lang = importDeclaration.substring(importDeclaration.lastIndexOf(".") + 1).toLowerCase();
	}

	/**
	 * @return String
	 */
	public String getCukeLang() {
		return lang;
	}

	/**
	 * @return String
	 */
	public String getMethodName() {
		return methodName.getFullyQualifiedName() + "()";
	}

	/**
	 * @return Type
	 */
	public Type getReturnType() {
		return returnType;
	}

	/**
	 * @return List<Statement>
	 */
	public List<Statement> getMethodBodyList() {
		return methodBodyList;
	}


	/**
	 * @param statement
	 * @return String
	 */
	public String getLambdaStep(Statement statement, Set<String> keywords) {
		if (statement instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			Expression expression = expressionStatement.getExpression();
			if (expression instanceof MethodInvocation) {
				MethodInvocation methodInvocation = (MethodInvocation) expression;
				String identifier = ((MethodInvocation) expression).getName().getIdentifier();
				if (keywords.contains(identifier)) {
					List<?> arguments = methodInvocation.arguments();
					for (Object object : arguments) {
						if (object instanceof StringLiteral) {
							String string = ((StringLiteral) object).getLiteralValue();
							return string;
						} else {
							break;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param method
	 * @param i18n 
	 * @return boolean
	 */
	public boolean isCukeLambdaExpr(MethodDeclaration method, Set<String> keywords) {
		@SuppressWarnings("unchecked")
		List<Statement> statements = method.getBody().statements();
		for (Statement statement : statements) {
			if (statement instanceof ExpressionStatement) {
				ExpressionStatement expressionStatement = (ExpressionStatement) statement;
				Expression expression = expressionStatement.getExpression();
				if (expression instanceof MethodInvocation) {
					String identifier = ((MethodInvocation) expression).getName().getIdentifier();
					if (keywords.contains(identifier)) {
						//we found a lamda
						return true;
					}
				}
			}
			
		}
		return false;
	}

	/**
	 * Check if import contains 'cucumber.api.java8'
	 * 
	 * @param IImportDeclaration[]
	 * @return boolean
	 */
	public boolean isJava8CukeAPI(IImportDeclaration[] allimports) {
		// Check if import contains 'cucumber.api.java8'
		return Arrays.asList(allimports).toString().matches(CONTAINS_JAVA8_CUKEAPI) ? true : false;
	}

	@Override
	public String toString() {
		return "Method-Details [ MethodName = " + methodName + "\n MethodReturnType = " + returnType
				+ "\n MethodBodyList = " + methodBodyList + "\n Language = " + lang + "]";
	}

}
