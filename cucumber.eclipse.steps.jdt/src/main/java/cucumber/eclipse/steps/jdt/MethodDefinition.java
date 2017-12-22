package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

/**
 * @author girija.panda@nokia.com 
 * Purpose : Used to get method details i.e.
 *         name/return-type/body from a source file Specially used to collect
 *         all steps from the java8-Lambda-Expressions used in
 *         methods/constructors of a class
 *
 */

public class MethodDefinition extends JavaParser {

	private SimpleName methodName;
	private Type returnType;

	// cucumber.api.java8.<lang>(ex. cucumber.api.java8.En)
	public static final String REGEX_JAVA8_CUKEAPI = "cucumber\\.api\\.java8\\.(.*)";
	public static final String CONTAINS_JAVA8_CUKEAPI = "(.*)cucumber\\.api\\.java8\\.(.*)";

	// Ex. Given( or Given<Space>(
	private final static String KEYWORD_SPACE_PARENTHESIS = "(Given|When|Then|And|But)[\\s]*[\\(]";
	private final static Pattern cukeKeywordPattern = Pattern.compile(KEYWORD_SPACE_PARENTHESIS);

	private static final String STARTSWITH_KEYWORD_PARENTHESIS = "^(Given|When|Then|And|But)[\\(][\\s\\S]*";

	private static final String REGEX_LAMBDA_STEP = "(\"[^\n]*\")[\\,][\\(]";
	private static final Pattern lambdaPattern = Pattern.compile(REGEX_LAMBDA_STEP);

	private static final String START_QUOTE = "\"";
	private static final String END_QUOTE_COMMA_PARENTHESIS = "\",(";

	private List<Statement> methodBodyList = new ArrayList<Statement>();
	private String methodBody;
	private String lang;
	private int lineNumber;

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
	 * Get Method Body And Line-Number
	 * 
	 * @param statement
	 * @return
	 */
	public String getBodyStatement(Statement statement) {
		this.methodBody = statement.toString();
		this.lineNumber = getLineNumber(statement);
		return methodBody;
	}

	/**
	 * @return int
	 */
	public int getCukeLineNumber() {
		return lineNumber;
	}

	/**
	 * @param lambdaExpr
	 * @return String
	 */
	public String getLambdaStep(String lambdaExpr) {

		String lambdaStep = lambdaExpr.trim();
		if (lambdaStep.matches(STARTSWITH_KEYWORD_PARENTHESIS)) {
			Matcher matcher = lambdaPattern.matcher(lambdaStep);
			while (matcher.find()) {
				String junkStep = matcher.group(0);
				if (junkStep.startsWith(START_QUOTE) && junkStep.endsWith(END_QUOTE_COMMA_PARENTHESIS)) {
					lambdaStep = junkStep.substring(1, junkStep.lastIndexOf(END_QUOTE_COMMA_PARENTHESIS));
					lambdaStep = lambdaStep.trim();
				}
			}
		}
		return lambdaStep;
	}

	/**
	 * @param methodBody
	 * @return boolean
	 */
	public boolean isCukeLambdaExpr(String methodBody) {
		// Matching the Method/Constructor-Block contains RegEx-Pattern:
		// Given(|When(|Then(|And(|But(
		// check if Method/Constructor-Block contains any RegEx-Pattern:
		// Given(|When(|Then(|And(|But(
		return cukeKeywordPattern.matcher(methodBody).find() ? true : false;
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
