package cucumber.eclipse.editor.editors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class DocumentUtil {

	public static String getDocumentLanguage(IDocument document) {
		String lang = "en"; // default
		try {
			IRegion lineInfo = document.getLineInformation(0);
			int length = lineInfo.getLength();
			int offset = lineInfo.getOffset();
			String line = document.get(offset, length);

			if (line.contains("language")) {
				int indexOf = line.indexOf(":");
				lang = line.substring((indexOf + 1)).trim();
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return lang;
	}
	
	public static IDocument read(InputStream in, String encoding) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		int n;
		byte[] b = new byte[16384];
		
		while ((n = in.read(b, 0, b.length)) != -1) {
			out.write(b, 0, n);
		}
		
		out.flush();
		
		return new Document(new String(out.toByteArray(), encoding));
	}
}
