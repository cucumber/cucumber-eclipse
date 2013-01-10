package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class GherkinConfiguration extends TextSourceViewerConfiguration {

	private GherkinKeywordScanner keywordScanner;
	private ColorManager colorManager;

	public GherkinConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE};
	}


	protected GherkinKeywordScanner getGherkinKeywordScanner() {
		if (keywordScanner == null) {
			keywordScanner = new GherkinKeywordScanner(colorManager);
			keywordScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(GherkinColors.DEFAULT))));
		}
		return keywordScanner;
	}


	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new GherkinDamagerRepairer(getGherkinKeywordScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	
		
		return reconciler;
	}
	
	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		final MultiPassContentFormatter formatter= new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
		formatter.setMasterStrategy(new GherkinFormattingStrategy());
		return formatter;
	}

}