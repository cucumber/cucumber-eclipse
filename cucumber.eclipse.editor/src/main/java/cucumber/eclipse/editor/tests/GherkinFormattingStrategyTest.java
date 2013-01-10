package cucumber.eclipse.editor.tests;

import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import cucumber.eclipse.editor.editors.GherkinFormatterUtil;

public class GherkinFormattingStrategyTest {
	@Ignore @Test public void testStringFormatting() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("unformatted stuff\n");
				String contents= buf.toString();


		String formatted= GherkinFormatterUtil.format( contents, 0, "\n");

		buf= new StringBuffer();
		buf.append("formatted stuff;\n");
		assertThat(buf.toString(), is(formatted));

	}

	@Ignore @Test public void testTextEdit() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("unformatted stuff\n");
		String contents= buf.toString();
		
		TextEdit edit= GherkinFormatterUtil.formatTextEdit(contents, 0, "\n");
		Document doc= new Document(contents);
		edit.apply(doc);
		String formatted= doc.get();

		buf= new StringBuffer();
		buf.append("formatted stuff;\n");
		String expected= buf.toString();
		assertThat(buf.toString(), is(formatted));
	}
}
