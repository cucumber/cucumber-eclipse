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

	public String getTextStatement(String language, String expression) {
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
	
	public Step matchSteps(String languageCode, Set<Step> steps, String currentLine) {

		//System.out.println("StepMatcher matchSteps() steps = " + steps);
		
		Matcher matcher = getBasicStatementMatcher(languageCode, currentLine);

		if (matcher.matches()) {

			String cukeStep = matcher.group(1);
			//System.out.println("StepMatcher matchSteps() cukeStep1 = " + cukeStep);
			// FIXME: Replace variables with (MPL - <p>) for now to allow them
			// to match steps
			// Should really read the whole scenario outline and sub in the
			// first scenario
			Matcher variableMatcher = variablePattern.matcher(cukeStep);
			cukeStep = variableMatcher.replaceAll("<p>");

			//System.out.println("StepMatcher matchSteps() cukeStep2 = " + cukeStep);

			for (Step step : steps) {

				// firstly, have to replace all non-parameter matching group
				// expressions to conform to normal regexp
				// e.g. (?:choiceone|choicetwo) -> (choiceone|choicetwo)
				// System.out.println("StepMatcher matchSteps() FOR START
				// ###########################################################");

				Matcher groupNonParameterMatcher = groupPatternNonParameterMatch.matcher(step.getText());
				while (groupNonParameterMatcher.find()) {
					step.setText(step.getText().replace(groupNonParameterMatcher.group(0),
							"(" + groupNonParameterMatcher.group(0).substring(3)));

					// System.out.println("WHILE-1 : StepMatcher matchSteps()
					// System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				}

				// for each group match, want to insert <p> as an option
				// e.g. (\\d+) becomes (<p>|\\d+)
				// e.g. (two|ten) becomes (<p>|two|ten)
				Matcher groupMatcher = groupPattern.matcher(step.getText());
				// System.out.println("GET-TEXT ="+step.getText());

				while (groupMatcher.find()) {
					// System.out.println("WHILE-2 : StepMatcher matchSteps()
					// groupMatcher.group(0) = " +groupMatcher.group(0));
					// System.out.println("WHILE-2 : StepMatcher matchSteps()
					// groupMatcher.group(0).substring(1) = "
					// +groupMatcher.group(0).substring(1));

					// Commented By Girija : To Avoid Multiple Appending of
					// '<P>|' in Content-Assistance-Step Proposals
					// step.setText(step.getText().replace(groupMatcher.group(0),
					// "(<p>|" + groupMatcher.group(0).substring(1)));

					// Updated By Girija
					// If Step doesn't starts-with '(<p>'
					// Then Append i.e. (<p>|RegEX)
					// Else Don't Append
					if (!groupMatcher.group(0).startsWith("(<p>|")) {

						// System.out.println("WHILE-2 : StepMatcher
						// matchSteps() groupMatcher.group(0) DOESNOT startsWith
						// <p>");
						step.setText(step.getText().replace(groupMatcher.group(0),
								"(<p>|" + groupMatcher.group(0).substring(1)));

						// System.out.println("WHILE-2 : StepMatcher
						// matchSteps() step-2 = " +step.getText());
						// System.out.println("############################################################################");
					} else
						break;
				}

				if (step.matches(cukeStep)) {
					return step;
				}

				// System.out.println("FOR : StepMatcher matchSteps() step-3 = "
				// +step.getText());
				// System.out.println("********************************************************************************");
			}

			// System.out.println("StepMatcher matchSteps() FOR END
			// ###########################################################");
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
