package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class FeatureFileUtil{

	static String getDocumentLanguage(IEditorPart editorPart) {
		String lang = null;
		try {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocumentProvider docProvider = editor.getDocumentProvider();
			IDocument doc = docProvider.getDocument(editorPart
					.getEditorInput());

			IRegion lineInfo = doc.getLineInformation(0);
			int length = lineInfo.getLength();
			int offset = lineInfo.getOffset();
			String line = doc.get(offset, length);

			if (line.contains("language")) {
				int indexOf = line.indexOf(":");
				lang = line.substring((indexOf + 1)).trim();
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return lang;
	}
}