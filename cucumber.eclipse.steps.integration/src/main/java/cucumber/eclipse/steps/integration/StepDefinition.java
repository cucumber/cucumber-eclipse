package cucumber.eclipse.steps.integration;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IResource;

import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

public class StepDefinition implements Serializable {

	private static final long serialVersionUID = 8357491927598467891L;
	
	private String text;
	private transient IResource source;
	private String sourcePath;
	private int lineNumber;
	private String lang;
	private transient Expression expression;
	private String jdtHandleIdentifier;
	private String label;
	
	//Added By Girija
	//For Reading Steps from External-ClassPath-JAR
	private String sourceName;
	private String packageName;
	
	//private String java8CukeSource;
	
	public String getText() {
		return text;
	}
	public void setText(String text) throws CucumberExpressionException {
		this.text = text;
		this.initExpressionFactory();
	}
	
	private void initExpressionFactory() {
		Locale locale = this.lang == null ? Locale.getDefault() : new Locale(this.lang);
		this.expression = new ExpressionFactory(new ParameterTypeRegistry(locale)).createExpression(text);
	}
	
	public IResource getSource() {
		// For marker, the plugin need to serialize StepDefinition
		// however IResource is not serializable
		// in this case we will retrieve it from its path
		if(source == null && sourcePath != null) {
			source = new ResourceHelper().find(this.sourcePath);
		}
		return source;
	}
	public void setSource(IResource source) {
		this.source = source;
		this.sourcePath = source.getFullPath().toString();
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public boolean matches(String stepDefinitionText) {
		if(this.expression == null) {
			this.initExpressionFactory();
		}
		List<Argument<?>> match = this.match(stepDefinitionText);
		return match != null;
	}
	
	public List<Argument<?>> match(String s) {
		try {
			return this.expression.match(s);
		} catch (Throwable e) {
			// if an exception occurs, this means the cucumber expression
			// have an error.
			e.printStackTrace();
			return null;
		}
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
	
	public String getJDTHandleIdentifier() {
		return jdtHandleIdentifier;
	}
	public void setJDTHandleIdentifier(String jdtHandleIdentifier) {
		this.jdtHandleIdentifier = jdtHandleIdentifier;
	}
	
	public String getLabel() {
		if(label == null) {
			label = this.getSource().getName() + ":" + this.lineNumber;
		}
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + lineNumber;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		if (lang == null) {
			if (other.lang != null)
				return false;
		} else if (!lang.equals(other.lang))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (sourceName == null) {
			if (other.sourceName != null)
				return false;
		} else if (!sourceName.equals(other.sourceName))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
	
}
