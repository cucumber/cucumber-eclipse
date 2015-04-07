package cucumber.eclipse.editor.editors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.junit.Test;

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
}
