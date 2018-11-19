package cucumber.eclipse.editor.editors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import cucumber.eclipse.steps.integration.Step;

public class StepMatcherTest {

	private StepMatcher stepMatcher = new StepMatcher();

	@Test
	public void simpleStepMatches() {

		Step s = createStep("^I run a test$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "When I run a test"));
	}

	@Test
	public void noStepMatches() {

		Step s1 = createStep("^I run a test$");
		Step s2 = createStep("^This is an other step$");

		assertNull(stepMatcher.matchSteps("en", new HashSet<Step>(Arrays.asList(s1, s2)), "When I write anything"));
	}
	
	@Test
	public void otherLanguageStepMatches() {

		Step s = createStep("^执行$");

		assertEquals(s, stepMatcher.matchSteps("zh-CN", Collections.singleton(s), "当执行"));
	}

	@Test
	public void scenarioOutlines() {

		Step s = createStep("^there are (\\d)* cucumbers$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Given there are <start> cucumbers"));
	}

	@Test
	public void scenarioOutlinesString() {

		Step s = createStep("^there are (\\w)* cucumbers$");
		Step s2 = createStep("^I should see the (.*) message$");
		Set<Step> steps = new HashSet<Step>();
		steps.add(s2);
		steps.add(s);

		assertEquals(s, stepMatcher.matchSteps("en", steps, "Given there are <start> cucumbers"));
	}

	@Test
	public void scenarioOutlinesAlias() {

		Step s = createStep("^there are (two|ten) cucumbers$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Given there are <cukes> cucumbers"));
	}

	@Test
	public void scenarioAlias() {

		Step s = createStep("^there are (two|ten) cucumbers$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Given there are two cucumbers"));
	}

	@Test
	public void scenarioOutlinesAliasMultiple() {

		Step s = createStep("^there are (two|ten) cucumbers and (five|fifteen) gherkins$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s),
				"Given there are <cukes> cucumbers and <gherks> gherkins"));
	}

	@Test
	public void scenarioAliasMultiple() {

		Step s = createStep("^there are (two|ten) cucumbers and (five|fifteen) gherkins$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s),
				"Given there are two cucumbers and fifteen gherkins"));
	}

	@Test
	public void scenarioOutlinesAliasNonMatching() {

		Step s = createStep("^there are (?:two|ten) cucumbers$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Given there are <cukes> cucumbers"));
	}

	@Test
	public void scenarioAliasNonMatching() {

		Step s = createStep("^there are (?:two|ten) cucumbers$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Given there are two cucumbers"));
	}

	@Test
	public void scenarioOutlinesAliasNonMatchingMultiple() {

		Step s = createStep("^there are (?:two|ten) cucumbers and (?:five|fifteen) gherkins$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s),
				"Given there are <cukes> cucumbers and <gherks> gherkins"));
	}

	@Test
	public void scenarioAliasNonMatchingMultiple() {

		Step s = createStep("^there are (?:two|ten) cucumbers and (?:five|fifteen) gherkins$");

		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s),
				"Given there are two cucumbers and fifteen gherkins"));
	}

	@Test
	public void cucumberExpressionStepMatches() {

		Step s = createStep("I run a {word}");
		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "When I run a test"));
		
		s = createStep("I expect {int} cucumber(s)");
		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Then I expect 5 cucumbers"));
		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "Then I expect 1 cucumber"));
	}
	
	@Test
	@Ignore("custom cucumber expressions are not detected but do not crash the editor anymore")
	public void customCucumberExpressionStepMatches() {

		Step s = createStep("I have a {color} ball");
		assertEquals(s, stepMatcher.matchSteps("en", Collections.singleton(s), "  Given I have a red ball"));
	}
	
	@Test
	public void customCucumberExpressionStepMatchesButDoesNotThrowException() {

		Step s = createStep("I have a {color} ball");
		try {
			stepMatcher.matchSteps("en", Collections.singleton(s), "  Given I have a red ball");
		} 
		catch (Exception e) {
			fail("Ooops ! I do not except exception");
		}
	}

	@Test
	public void shouldNotThrowExceptionWhenPatternIsMalformed() {
		try {
		Step s = createStep("^(<p>|?>region|regions) of type (.*) (?>are|is) covering all references$");
		stepMatcher.matchSteps("en", Collections.singleton(s), "  Given I have a red ball");
		} 
		catch (Exception e) {
			fail("Ooops ! I do not except exception");
		}
	}
	
	private Step createStep(String text) {

		Step s = new Step();
		s.setText(text);
		return s;
	}

	@Test
	public void shouldReturnExpressionWithoutStartingKeyword() {
		String statement = stepMatcher.getTextStatement("en", "Given I have a cat");
		assertThat(statement, equalTo("I have a cat"));
		
		statement = stepMatcher.getTextStatement("en", "When I carress him");
		assertThat(statement, equalTo("I carress him"));
		
		statement = stepMatcher.getTextStatement("en", "Then he purrs");
		assertThat(statement, equalTo("he purrs"));
		
		statement = stepMatcher.getTextStatement("en", "And I am happy");
		assertThat(statement, equalTo("I am happy"));
	}
}
