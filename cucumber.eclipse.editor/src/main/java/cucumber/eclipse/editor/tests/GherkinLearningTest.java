package cucumber.eclipse.editor.tests;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;
import org.junit.Test;

import gherkin.formatter.Formatter;
import gherkin.parser.Parser;
import gherkin.formatter.PrettyFormatter;

public class GherkinLearningTest {
	@Test
	public void format() throws UnsupportedEncodingException {
		String gherkin = GherkinTestFixtures.unformatted_feature;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(output);
		Formatter formatter = new PrettyFormatter(out, true, false);

		Parser parser = new Parser(formatter);
		parser.parse(gherkin, "", 0);

		out.flush();

		String should_be = GherkinTestFixtures.formatted_feature;

		assertThat(output.toString(), is(should_be));

	}

}
