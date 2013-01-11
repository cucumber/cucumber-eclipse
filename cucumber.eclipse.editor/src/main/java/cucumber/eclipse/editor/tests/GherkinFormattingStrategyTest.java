package cucumber.eclipse.editor.tests;

import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import cucumber.eclipse.editor.editors.GherkinFormatterUtil;

public class GherkinFormattingStrategyTest {
	@Test public void testStringFormatting() throws Exception {

		String formatted= GherkinFormatterUtil.format( GherkinTestFixtures.unformatted_feature );
		assertThat(GherkinTestFixtures.formatted_feature, is(formatted));

	}

	@Ignore @Test public void testTextEdit() throws Exception {
		
		TextEdit edit= GherkinFormatterUtil.formatTextEdit( GherkinTestFixtures.unformatted_feature, 0, "\n");
		Document doc= new Document(GherkinTestFixtures.unformatted_feature);
		edit.apply(doc);
		String formatted = doc.get();

		assertThat(GherkinTestFixtures.formatted_feature, is(formatted));
	}
}
