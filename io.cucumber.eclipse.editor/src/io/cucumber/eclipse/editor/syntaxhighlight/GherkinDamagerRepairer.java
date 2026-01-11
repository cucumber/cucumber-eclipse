package io.cucumber.eclipse.editor.syntaxhighlight;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.gherkin.GherkinDialect;

/**
 * Damage Repairer with special handling to update keywords on language changes
 * 
 * @author christoph
 *
 */
public class GherkinDamagerRepairer extends DefaultDamagerRepairer {

	private String currentLanguage;

	/**
	 * @param scanner keywordscanner to use
	 */
	public GherkinDamagerRepairer(GherkinKeywordScanner scanner) {
		super(scanner);
	}

	@Override
	public void setDocument(IDocument document) {
		updateLanguage(GherkinEditorDocumentManager.get(document, true));
		super.setDocument(document);
	}

	@Override
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
		IRegion region = super.getDamageRegion(partition, e, documentPartitioningChanged);
		IDocument doc = e.getDocument();
		GherkinEditorDocument gherkinDocument = GherkinEditorDocumentManager.get(doc, true);
		GherkinDialect dialect = gherkinDocument.getDialect();
		String language = dialect.getLanguage();
		if (!language.equals(currentLanguage)) {
			updateLanguage(gherkinDocument);
			return new Region(0, doc.getLength());
		}
		return region;
	}

	private void updateLanguage(GherkinEditorDocument gherkinDocument) {
		String language = gherkinDocument.getDialect().getLanguage();
		currentLanguage = language;
		GherkinKeywordScanner scanner = (GherkinKeywordScanner) fScanner;
		scanner.configureRules(gherkinDocument);
	}

}
