package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

public class GherkinDamagerRepairer extends DefaultDamagerRepairer {
	private static String code = "en";

	public GherkinDamagerRepairer(ITokenScanner scanner) {
		super(scanner);
	}

	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e,
			boolean documentPartitioningChanged) {
		IRegion damageRegion = super.getDamageRegion(partition, e,
				documentPartitioningChanged);

		if (fScanner instanceof GherkinKeywordScanner) {
			String newCode = determineGherkinLanguageMode(fDocument);
			if (!newCode.equals(code)) {
				code = newCode;
				GherkinKeywordScanner.setCode(code);
				((GherkinKeywordScanner) fScanner).configureRules();
				damageRegion = new Region(0, fDocument.getLength());
			}
		}

		return damageRegion;
	}

	private String determineGherkinLanguageMode(IDocument document) {
		String code = "en"; // default
		try {

			IRegion lineInformation = document.getLineInformation(0);
			int length = lineInformation.getLength();
			int offset = lineInformation.getOffset();
			String string = document.get(offset, length);
			if (string.contains("language")) {
				int indexOf = string.indexOf(":");
				code = string.substring((indexOf + 1)).trim();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return code;
	}
}
