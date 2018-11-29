package cucumber.eclipse.editor.editors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.junit.Test;

import gherkin.formatter.model.BasicStatement;

public class GherkinModelTest {

    @Test
    public void stepContainerFoldRangeExtendsToLineFollowingLastStep() throws BadLocationException {        
        String source = "Feature: x\n"
                + "\n"
                + "  Scenario: 1\n" // line 2
                + "    Given y with\n"
                + "      \"\"\"\n"
                + "      a\n"
                + "      \"\"\"\n"
                + "\n" // line 7
                + "  Scenario: 2\n"
                + "    Given z\n";
        Document document = new Document(source);
        GherkinModel model = new GherkinModel();
        
        model.updateFromDocument(document);
        Position range = model.getFoldRanges().get(1);
        
        assertThat("offset", range.getOffset(), is(document.getLineOffset(2)));
        assertThat("range", range.getLength(), is(document.getLineOffset(7) - document.getLineOffset(2)));
    }
    
    @Test
    public void scenarioOutlineFoldRangeExtendsToLineFollowingLastExampleRow() throws BadLocationException {        
        String source = "Feature: x\n"
                + "\n"
                + "  Scenario Outline: 1\n" // line 2
                + "    Given y\n"
                + "\n"
                + "    Examples:\n"
                + "      | a | b |\n"
                + "      | 1 | 2 |\n"
                + "\n" // line 8
                + "  Scenario: 2\n"
                + "    Given z\n";
        Document document = new Document(source);
        GherkinModel model = new GherkinModel();
        
        model.updateFromDocument(document);
        Position range = model.getFoldRanges().get(1);
        
        assertThat("offset", range.getOffset(), is(document.getLineOffset(2)));
        assertThat("range", range.getLength(), is(document.getLineOffset(8) - document.getLineOffset(2)));
    }
    
    @Test
    public void examplesFoldRangeExtendsToLineFollowingLastRow() throws BadLocationException {        
        String source = "Feature: x\n"
                + "\n"
                + "  Scenario Outline: 1\n"
                + "    Given y\n"
                + "\n"
                + "    Examples:\n" // line 5
                + "      | a | b |\n"
                + "      | 1 | 2 |\n"
                + "\n" // line 8
                + "  Scenario: 2\n"
                + "    Given z\n";
        Document document = new Document(source);
        GherkinModel model = new GherkinModel();
        
        model.updateFromDocument(document);
        Position range = model.getFoldRanges().get(2);
        
        assertThat("offset", range.getOffset(), is(document.getLineOffset(5)));
        assertThat("range", range.getLength(), is(document.getLineOffset(8) - document.getLineOffset(5)));
    }
    
    @Test
    public void featureElementHasAttachedChildren() throws BadLocationException {        
        String source = "Feature: x\n"
                + "\n"
                + "  Background:\n"
                + "    When x\n"
                + "\n"
                + "  Scenario Outline: 1\n"
                + "    Given y\n"
                + "\n"
                + "    Examples:\n"
                + "      | a | b |\n"
                + "      | 1 | 2 |\n"
                + "\n"
                + "  Scenario: 2\n"
                + "    Given z\n";
        Document document = new Document(source);
        GherkinModel model = new GherkinModel();
        
        model.updateFromDocument(document);
        PositionedElement feature = model.getFeatureElement();
        
        assertThat("feature.children.size",
                feature.getChildren().size(), is(3));
        assertThat("feature.children[0].background",
                feature.getChildren().get(0).isBackground(), is(true));
        assertThat("feature.children[1].scenarioOutline",
                feature.getChildren().get(1).isScenarioOutline(), is(true));
        assertThat("feature.children[2].scenario",
                feature.getChildren().get(2).isScenario(), is(true));
        assertThat("feature.children[0].children.size",
                feature.getChildren().get(0).getChildren().size(), is(1));
        assertThat("feature.children[0].children[0].step",
                feature.getChildren().get(0).getChildren().get(0).isStep(), is(true));
        assertThat("feature.children[1].children.size",
                feature.getChildren().get(1).getChildren().size(), is(1));
        assertThat("feature.children[1].children[0].step",
                feature.getChildren().get(1).getChildren().get(0).isStep(), is(true));
        assertThat("feature.children[2].children.size",
                feature.getChildren().get(2).getChildren().size(), is(1));
        assertThat("feature.children[2].children[0].step",
                feature.getChildren().get(2).getChildren().get(0).isStep(), is(true));
    }
    
	@Test
	public void getStepElementReturnsElement() throws BadLocationException {
		String source = "Feature: x\n" + "\n"
				+ "  Scenario: 1\n"
				+ "    Given y\n" // line 3
				+ "    And z\n";

		Document document = new Document(source);
		GherkinModel model = new GherkinModel();
		model.updateFromDocument(document);

		int stepOffset = source.indexOf("Given");
		BasicStatement statement = model.getStepElement(stepOffset)
				.getStatement();

		assertThat(statement.getName(), is("y"));
	}
	
	@Test
	public void supportI18NFeature() throws BadLocationException {
		String source = "# language: fr\n"
				+ "Fonctionnalité: x\n" + "\n"
				+ "  Scénario: 1\n"
				+ "    Soit y\n" // line 3
				+ "    Alors z\n";

		Document document = new Document(source);
		GherkinModel model = new GherkinModel();
		model.updateFromDocument(document);

		int stepOffset = source.indexOf("Soit");
		BasicStatement statement = model.getStepElement(stepOffset)
				.getStatement();

		assertThat(statement.getName(), is("y"));
	}
	
}
