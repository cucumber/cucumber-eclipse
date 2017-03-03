package cucumber.eclipse.editor.editors;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import cucumber.eclipse.steps.integration.Step;
import gherkin.I18n;

class StepMatcher {
	private Pattern variablePattern = Pattern.compile("<([^>]+)>");
	private Pattern groupPatternNonParameterMatch = Pattern.compile("(\\(\\?:.+?\\))");
	private Pattern groupPattern = Pattern.compile("(\\(.+?\\))");

	Step matchSteps(String languageCode, Set<Step> steps, String currentLine) {
		Pattern cukePattern = getLanguageKeyWordMatcher(languageCode);

		if (cukePattern == null)
			return null;

		Matcher matcher = cukePattern.matcher(currentLine);
		if (matcher.matches()) {
			String cukeStep = matcher.group(1);

			// FIXME: Replace variables with (MPL - <p>) for now to allow them
			// to match steps
			// Should really read the whole scenario outline and sub in the
			// first scenario
			Matcher variableMatcher = variablePattern.matcher(cukeStep);
			cukeStep = variableMatcher.replaceAll("<p>");
						
			for (Step step : steps) {
				// firstly, have to replace all non-parameter matching group
				// expressions to conform to normal regexp
				// e.g. (?:choiceone|choicetwo) -> (choiceone|choicetwo)
				Matcher groupNonParameterMatcher = groupPatternNonParameterMatch.matcher(step.getText());
				while (groupNonParameterMatcher.find()) {
					step.setText(step.getText().replace(groupNonParameterMatcher.group(0),
							"(" + groupNonParameterMatcher.group(0).substring(3)));
				}

				// for each group match, want to insert <p> as an option
				// e.g. (\\d+) becomes (<p>|\\d+)
				// e.g. (two|ten) becomes (<p>|two|ten)
//				if (!step.getText().contains("<p>|")) { 
//					Matcher groupMatcher = groupPattern.matcher(step.getText());
//					while (groupMatcher.find()) {
//						step.setText(step.getText().replace(groupMatcher.group(0),
//								"(<p>|" + groupMatcher.group(0).substring(1)));
//					}
//				}
//								
				step.setText(step.getText().replace("\\\\d", "\\d"));
				step.setText(step.getText().replace("\\\"", "\""));
				step.setText(step.getText().replace("\\\\.", "."));
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
}
