package cucumber.eclipse.editor.editors;

import gherkin.I18n;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import cucumber.eclipse.steps.integration.Step;

class StepMatcher{
	private Pattern variablePattern = Pattern.compile("<([^>]+)>");

	Step matchSteps(String languageCode, Set<Step> steps, String currentLine) {
		Pattern cukePattern = getLanguageKeyWordMatcher(languageCode);

		if (cukePattern == null)
			return null;

		Matcher matcher = cukePattern.matcher(currentLine);
		if (matcher.matches()) {
			String cukeStep = matcher.group(1);

			// FIXME: Replace variables with 0 for now to allow them to
			// match steps
			// Should really read the whole scenario outline and sub in the
			// first scenario
			Matcher variableMatcher = variablePattern.matcher(cukeStep);
			cukeStep = variableMatcher.replaceAll("0");

			for (Step step : steps) {
				if (step.matches(cukeStep)) {
					return step;
				}
			}

		}
		return null;
}

private Pattern getLanguageKeyWordMatcher(String languageCode) {
	try {
		if (languageCode == null) {
			languageCode = "en";
		} else {
			languageCode = languageCode.toLowerCase();
		}
		I18n i18n = new I18n(languageCode);
		
		StringBuilder sb = new StringBuilder();
		sb.append("(?:");
		String delim = "";
	
		for(String keyWord : i18n.getCodeKeywords()) {
			sb.append(delim).append(keyWord);
			delim = "|";
		}
	
		return Pattern.compile((sb.append(") (.*)$").toString()));
	} catch(NullPointerException e) {
		e.printStackTrace();
		return null;
	} catch(PatternSyntaxException e) {
		e.printStackTrace();
		return null;
	}
}
}