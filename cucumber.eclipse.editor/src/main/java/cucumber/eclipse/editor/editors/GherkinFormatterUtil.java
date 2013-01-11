package cucumber.eclipse.editor.editors;

import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.parser.Parser;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.eclipse.text.edits.TextEdit;

public class GherkinFormatterUtil {

	public static String format(String contents) {
		// set up
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(output);
		Formatter formatter = new PrettyFormatter(out, true, false);

		// parse 
		new Parser(formatter).parse(contents, "", 0);

		out.flush();
		return output.toString();
	}

	public static TextEdit formatTextEdit(String contents, int i, String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
