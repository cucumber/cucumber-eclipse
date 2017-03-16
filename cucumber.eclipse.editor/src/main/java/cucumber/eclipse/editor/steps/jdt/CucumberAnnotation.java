package cucumber.eclipse.editor.steps.jdt;

/**
 * @author girija.panda@nokia.com
 * 
 * Purpose: Redefining same class which already exists in 'cucumber.eclipse.steps.jdt.CucumberAnnotation'
 * 			in order to avoid plugin-dependency conflicts. 
 * 			Used for the Reading/Reusing Step-Definitions from External ClassPath JAR 
 */
public class CucumberAnnotation {

	private String annotation;
	
	private String lang;

	public CucumberAnnotation(String annotation, String lang) {
		super();
		this.annotation = annotation;
		this.lang = lang;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getLang() {
		return lang;
	}

	@Override
	public String toString() {
		return "CucumberAnnotation [annotation=" + annotation + ", lang=" + lang + "]";
	}
	
	
	
}
