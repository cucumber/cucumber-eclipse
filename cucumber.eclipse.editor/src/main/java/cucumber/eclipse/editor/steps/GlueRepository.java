package cucumber.eclipse.editor.steps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.Step;
import gherkin.I18n;


public class GlueRepository {
	
	public static final GlueRepository INSTANCE = new GlueRepository();
	
	
	private Map<GherkinStepWrapper, Step> glueStorage = new HashMap<GherkinStepWrapper, Step>();
	
	private GlueRepository() {
	}
	
	public Glue get(GherkinStepWrapper gherkinStep) {
		Step stepDefinition = this.glueStorage.get(gherkinStep);
		if(stepDefinition == null) {
			return null;
		}
		return new Glue(gherkinStep, stepDefinition);
	}
	
	public void add(GherkinStepWrapper gherkinStep, Step stepDefinition) {
		this.glueStorage.put(gherkinStep, stepDefinition);
	}
	
	public Set<IFile> getGherkinSources() {
		Set<IFile> gherkinSources = new HashSet<IFile>();
		
		for (GherkinStepWrapper gherkinStep : this.glueStorage.keySet()) {
			gherkinSources.add((IFile) gherkinStep.getSource());
		}
		
		return gherkinSources;
	}
	
	public Set<IFile> getStepDefinitionsSources() {
		Set<IFile> stepDefinitionsSources = new HashSet<IFile>();
		
		for (Step stepDefinition : this.glueStorage.values()) {
			stepDefinitionsSources.add((IFile) stepDefinition.getSource());
		}
		
		return stepDefinitionsSources;
	}
	
	/**
	 * @param fromGherkinStepText
	 * @return the step definition related to this gherkin step. Or, null when not found
	 */
	public Glue findGlue(String fromGherkinStepText) {
		Entry<GherkinStepWrapper,Step> glue = null;
		
		Set<Entry<GherkinStepWrapper,Step>> entrySet = this.glueStorage.entrySet();
		for (Entry<GherkinStepWrapper, Step> entry : entrySet) {
			
			GherkinStepWrapper gherkinStepWrapper = entry.getKey();
			gherkin.formatter.model.Step gherkinStep = gherkinStepWrapper.getStep();
			
			String regex = Pattern.quote(gherkinStep.getKeyword()) + "[ ]*" + Pattern.quote(gherkinStep.getName());
			
			if(Pattern.matches(regex, fromGherkinStepText)) {
				glue = entry;
				break;
			}
		}
		
		if(glue == null) {
			return null;
		}
		
		return new Glue(glue.getKey(), glue.getValue());
	}
	
	public void clean() {
		this.glueStorage.clear();
	}
	
	/** Get the text statement of a gherkin step.
	 * For example, with the step "Given I love cats" will return "I love cats"
	 * @param language the document language
	 * @param expression a gherkin step expression
	 * @return the text part of the gherkin step expression
	 */
	protected String getTextStatement(String language, String expression) {
		Matcher matcher = getBasicStatementMatcher(language, expression);
		if(matcher == null) {
			return null;
		}
		if(matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
	
	/**
	 * Get a matcher to ensure text starts with a basic step keyword : Given, When,
	 * Then, etc
	 * 
	 * @param language the document language
	 * @param text the text to match
	 * @return a matcher
	 */
	private Matcher getBasicStatementMatcher(String language, String text) {
		Pattern cukePattern = getLanguageKeyWordMatcher(language);

		if (cukePattern == null)
			return null;

		return cukePattern.matcher(text.trim());
	}
	
	protected Pattern getLanguageKeyWordMatcher(String languageCode) {
		try {
			if (languageCode == null) {
				languageCode = "en";
			}
			I18n i18n = new I18n(languageCode);

			StringBuilder sb = new StringBuilder();
			sb.append("(?:");
			String delim = "";

			for (String keyWord : i18n.getStepKeywords()) {
				sb.append(delim).append(Pattern.quote(keyWord));
				delim = "|";
			}

			return Pattern.compile((sb.append(")(.*)$").toString()));

		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public class Glue {

		private GherkinStepWrapper gherkinStepWrapper;
		private Step stepDefinition;

		public Glue(GherkinStepWrapper gherkinStepWrapper, Step stepDefinition) {
			super();
			this.gherkinStepWrapper = gherkinStepWrapper;
			this.stepDefinition = stepDefinition;
		}

		public GherkinStepWrapper getGherkinStepWrapper() {
			return gherkinStepWrapper;
		}

		public Step getStepDefinition() {
			return stepDefinition;
		}

	}
	
}