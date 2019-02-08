package cucumber.eclipse.editor.steps;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import cucumber.eclipse.steps.integration.GherkinStepWrapper;
import cucumber.eclipse.steps.integration.Glue;
import cucumber.eclipse.steps.integration.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.model.Step;


public class GlueRepository implements Externalizable {
	
	private static final long serialVersionUID = -3784224573706686779L;

	
	private final Map<GherkinStepWrapper, StepDefinition> glues = new HashMap<GherkinStepWrapper, StepDefinition>();
	
	protected GlueRepository() {
	}
	
	public Glue get(GherkinStepWrapper gherkinStep) {
		StepDefinition stepDefinition = this.glues.get(gherkinStep);
		if(stepDefinition == null) {
			return null;
		}
		return new Glue(gherkinStep, stepDefinition);
	}
	
	public Glue add(GherkinStepWrapper gherkinStep, StepDefinition stepDefinition) {
		this.glues.put(gherkinStep, stepDefinition);
		return new Glue(gherkinStep, stepDefinition);
	}
	
	public Set<IFile> getGherkinSources() {
		Set<IFile> gherkinSources = new HashSet<IFile>();
		
		for (GherkinStepWrapper gherkinStep : this.glues.keySet()) {
			gherkinSources.add((IFile) gherkinStep.getSource());
		}
		
		return gherkinSources;
	}
	
	public Set<IFile> getStepDefinitionsSources() {
		Set<IFile> stepDefinitionsSources = new HashSet<IFile>();
		
		for (StepDefinition stepDefinition : this.glues.values()) {
			stepDefinitionsSources.add((IFile) stepDefinition.getSource());
		}
		
		return stepDefinitionsSources;
	}
	
	/** Find the glue for a gherkin statement
	 * @param fromGherkinStepText a gherkin expression
	 * @return the step definition related to this gherkin step. Or, null when not found
	 */
	public Glue findGlue(String fromGherkinStepText) {
		Entry<GherkinStepWrapper,StepDefinition> glue = null;
		
		Set<Entry<GherkinStepWrapper,StepDefinition>> entrySet = this.glues.entrySet();
		for (Entry<GherkinStepWrapper, StepDefinition> entry : entrySet) {
			
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
		this.glues.clear();
	}
	
	public void clean(Step step) {
		for (GherkinStepWrapper gherkinStepWrapper : this.glues.keySet()) {
			Integer lineNumber = gherkinStepWrapper.getStep().getLine();
			
			if(step.getLine().equals(lineNumber)) {
				this.glues.remove(gherkinStepWrapper);
				break;
			}
		}
	}

	public void clean(IResource gherkinFile) {
		List<GherkinStepWrapper> toRemove = new ArrayList<GherkinStepWrapper>();
		for (GherkinStepWrapper gherkinStepWrapper : this.glues.keySet()) {
			if(gherkinStepWrapper.getSource().equals(gherkinFile)) {
				toRemove.add(gherkinStepWrapper);
			}
		}
		for (GherkinStepWrapper gherkinStepWrapperInDeletion : toRemove) {
			this.glues.remove(gherkinStepWrapperInDeletion);
		}
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
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		glues.clear();
		int items = in.readInt();
		for (int i = 0; i < items; i++) {
			GherkinStepWrapper wrapper = (GherkinStepWrapper) in.readObject();
			glues.put(wrapper, StorageHelper.readStepDefinition(in));
		}
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(glues.size());
		for (Entry<GherkinStepWrapper, StepDefinition> entry : glues.entrySet()) {
			out.writeObject(entry.getKey());
			StorageHelper.writeStepDefinition(entry.getValue(), out);
		}
		
	}
}