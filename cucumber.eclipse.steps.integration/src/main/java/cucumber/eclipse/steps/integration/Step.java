package cucumber.eclipse.steps.integration;

import java.util.List;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IResource;

import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.UndefinedParameterTypeException;

public class Step {

	private String text;
	private IResource source;
	private int lineNumber;
	private String lang;
	private Expression expression;
	
	//Added By Girija
	//For Reading Steps from External-ClassPath-JAR
	private String sourceName;
	private String packageName;
	
	//private String java8CukeSource;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
		Locale locale = this.lang == null ? Locale.getDefault() : new Locale(this.lang);
		try {
			this.expression = new ExpressionFactory(new ParameterTypeRegistry(locale)).createExpression(text);
		}
		
		catch (UndefinedParameterTypeException e) {
			// the cucumber expression have a custom parameter type
			// without definition.
			// For example, "I have a {color} ball" 
			// But the "color" parameter type was not register 
			// thanks to a TypeRegistryConfigurer.
			this.expression = null;
		}
		catch (PatternSyntaxException e) {
			// This fix #286
			// the regular expression is wrong
			// we do not expect to match something with it
			// but we do not want to crash the F3
			this.expression = null;
		}
	}
	public IResource getSource() {
		return source;
	}
	public void setSource(IResource source) {
		this.source = source;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public boolean matches(String s) {
		if(this.expression == null)
			return false;
		List<Argument<?>> match = this.expression.match(s);
		return match != null;
	}
	
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	
	
	/*//For Java8-Cuke-Step-Definition file
	public void setJava8CukeSource(String java8CukeSource) {
		this.java8CukeSource = java8CukeSource;
	}
	
	public String getJava8CukeSource() {
		return java8CukeSource;
	}*/
	
	
	
	
	//Added By Girija
	//Newly Added Below Methods For Reading Steps from External-ClassPath-JAR
	//Set SourceName
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
	//Get SourceName
	public String getSourceName() {
		return sourceName;
	}
	
	//Set PackageName
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	//Get PackageName
	public String getPackageName() {
		return packageName;
	}
	
	
	@Override
	public String toString() {
		
		//For Steps from Current-Project
		if(lineNumber != 0)
				return "Step [text=" + text + ", source=" + source + ", lineNumber="+ lineNumber +"]";
			
		//For Steps From External-ClassPath JAR
		else		
			return "Step [text=" + text + ", source=" + sourceName+", package="+ packageName +"]";
	}
	
}
