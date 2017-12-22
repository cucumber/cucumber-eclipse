package cucumber.eclipse.steps.integration;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;

public class Step {

	private String text;
	private IResource source;
	private int lineNumber;
	private String lang;
	private Pattern compiledText;
	
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
		this.compiledText = Pattern.compile(text);
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
		return compiledText.matcher(s).matches();
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
