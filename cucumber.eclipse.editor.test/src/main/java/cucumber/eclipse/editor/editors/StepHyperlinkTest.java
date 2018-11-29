package cucumber.eclipse.editor.editors;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.junit.Before;
import org.junit.Test;

import cucumber.eclipse.steps.integration.Step;

public class StepHyperlinkTest {

	private StepHyperlink stepHyperlink;
	IRegion region;
	
	@Before
	public void setUp() {
		region = new Region(0, 10);
		Step step = new Step();
		step.setText("Given I have a cat");
		stepHyperlink = new StepHyperlink(region, step);
	}
	
	@Test
	public void shouldHaveATypeLabel() {
		assertThat(stepHyperlink.getTypeLabel(), equalTo("Gherkin step"));
	}

	@Test
	public void shouldHaveAnAlternateText() {
		assertThat(stepHyperlink.getHyperlinkText(), equalTo("Open step definition"));
	}
	
	@Test
	public void shouldReturnTheExpectedRegion() {
		assertThat(stepHyperlink.getHyperlinkRegion(), equalTo(region));
	}
	
	// should have UI automate test to validate the step hyperlink open.
}
