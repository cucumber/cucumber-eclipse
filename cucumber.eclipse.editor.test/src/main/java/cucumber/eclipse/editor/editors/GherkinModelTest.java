package cucumber.eclipse.editor.editors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.junit.Test;

public class GherkinModelTest {

    @Test
    public void stepContainerFoldRangeExtendsToNextStepContainer() throws BadLocationException {        
        String source = "Feature: x\n"
                + "\n"
                + "  Scenario: 1\n" // line 2
                + "    Given y\n"
                + "\n"
                + "  Scenario: 2\n" // line 5
                + "    Given z\n";
        Document document = new Document(source);
        GherkinModel model = new GherkinModel();
        
        model.updateFromDocument(document);
        Position range = model.getFoldRanges().get(0);
        
        assertThat("offset", range.getOffset(), is(document.getLineOffset(2)));
        assertThat("range", range.getLength(), is(document.getLineOffset(5) - document.getLineOffset(2)));
    }
    
    @Test
    public void examplesFoldRangeExtendsToNextStepContainer() throws BadLocationException {        
        String source = "Feature: x\n"
                + "\n"
                + "  Scenario Outline: 1\n"
                + "    Given y\n"
                + "\n"
                + "    Examples:\n" // line 5
                + "      | a | b |\n"
                + "      | 1 | 2 |\n"
                + "\n"
                + "  Scenario: 2\n" // line 9
                + "    Given z\n";
        Document document = new Document(source);
        GherkinModel model = new GherkinModel();
        
        model.updateFromDocument(document);
        Position range = model.getFoldRanges().get(0);
        
        assertThat("offset", range.getOffset(), is(document.getLineOffset(5)));
        assertThat("range", range.getLength(), is(document.getLineOffset(9) - document.getLineOffset(5)));
    }
}
