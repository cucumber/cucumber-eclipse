package cucumber.eclipse.editor.tests;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
//import static org.hamcrest.Matchers.*;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.editors.GherkinFormatterUtil;
import cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants;
import cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants.CucumberIndentationStyle;

public class GherkinFormattingStrategyTest {

    public final String feature_with_comments_unformatted_and_formatted =
		"Feature: feature name\n"
		+ "\n"
		+ "  Scenario: scenario 1\n"
		+ "    When some action\n"
		+ "    # Then some validation\n"
		+ "    # And some more validation\n";


    @Test
    public void testStringFormatting() throws Exception {
		String formatted= GherkinFormatterUtil.format(GherkinTestFixtures.unformatted_feature);
		assertThat(GherkinTestFixtures.formatted_feature, is(formatted));	

		// Make sure that formatting again does not change the result.
		assertThat(GherkinTestFixtures.formatted_feature, is(GherkinFormatterUtil.format(formatted)));
    }

    @Test
    public void testStringFormattingFourSpaces() throws Exception {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_INDENTATION_STYLE, CucumberIndentationStyle.FOUR_SPACES.getValue());
		String formatted= GherkinFormatterUtil.format(GherkinTestFixtures.unformatted_feature);
		assertThat(GherkinTestFixtures.formatted_feature_4_spaces, is(formatted));
		
		// Make sure that formatting again does not change the result.
		assertThat(GherkinTestFixtures.formatted_feature_4_spaces, is(GherkinFormatterUtil.format(formatted)));

		store.setValue(ICucumberPreferenceConstants.PREF_INDENTATION_STYLE, CucumberIndentationStyle.TWO_SPACES.getValue());
    }

    @Test
    public void testStringFormattingTabs() throws Exception {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_INDENTATION_STYLE, CucumberIndentationStyle.TABS.getValue());
		String formatted= GherkinFormatterUtil.format(GherkinTestFixtures.unformatted_feature);
		assertThat(GherkinTestFixtures.formatted_feature_tabs, is(formatted));	
		store.setValue(ICucumberPreferenceConstants.PREF_INDENTATION_STYLE, CucumberIndentationStyle.TWO_SPACES.getValue());
    }
    
    @Test
    public void testStringFormattingEndingWithComments() throws Exception {    	
		String formatted= GherkinFormatterUtil.format(feature_with_comments_unformatted_and_formatted);
		assertThat(formatted, is(feature_with_comments_unformatted_and_formatted));	
    }

    
    @Ignore
    @Test 
    public void testTextEdit() throws Exception {	
		TextEdit edit= GherkinFormatterUtil.formatTextEdit( GherkinTestFixtures.unformatted_feature, 0, "\n");
		Document doc= new Document(GherkinTestFixtures.unformatted_feature);
		edit.apply(doc);
		String formatted = doc.get();
	
		assertThat(GherkinTestFixtures.formatted_feature, is(formatted));
    }
}
