package cucumber.eclipse.steps.jdt;

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
		return "CucumberAnnotation [annotation=" + annotation + ", lang="
				+ lang + "]";
	}
	
	
	
}
