package cucumber.eclipse.editor.editors;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import cucumber.eclipse.steps.integration.Step;
import gherkin.I18n;

class StepMatcher {
	private Pattern variablePattern = Pattern.compile("<([^>]+)>");
	private Pattern aliasPattern = Pattern.compile("\\(([\\w|/]+)\\)");

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
				// for each alias match, want to insert 0 as an option
				// e.g. (two|ten) becomes (0|two|ten)
				Matcher aliasMatcher = aliasPattern.matcher(step.getText());
				while (aliasMatcher.find()) {
					step.setText(
							step.getText().replace(aliasMatcher.group(0), "(0|" + aliasMatcher.group(0).substring(1)));
				}

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
