package cucumber.eclipse.editor.editors;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	
	private Step createStep(String text) {
		
		Step s = new Step();
		s.setText(text);
		return s;
	}
	
}

