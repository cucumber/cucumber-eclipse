package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class GherkinPartitioner extends FastPartitioner {

	private static String DEFAULT_EN = "en";
	
	public GherkinPartitioner(IPartitionTokenScanner scanner,
			String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}
	
	@Override
	public void connect(IDocument document, boolean delayInitialization) {
		super.connect(document, delayInitialization);
		// set language if there is a language line
		setGherkinLanguageMode(document);
	}

	public void setGherkinLanguageMode(IDocument document) {
		String code = determineGherkinLanguageMode(document);
		GherkinKeywordScanner.setCode(code);
	}

	private String determineGherkinLanguageMode(IDocument document) {
		String code = DEFAULT_EN; //default
		try {

			IRegion lineInformation = document.getLineInformation(0);
			int length = lineInformation.getLength();
			int offset = lineInformation.getOffset();
			String string = document.get(offset, length);
			if (string.contains("language")) {
				int indexOf = string.indexOf(":");
				code = string.substring((indexOf + 1)).trim();				
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return code;
	}
	
	public IRegion documentChanged2(DocumentEvent e) {
		IRegion documentChanged2 = super.documentChanged2(e);
		
		return documentChanged2;
		
	}


}
