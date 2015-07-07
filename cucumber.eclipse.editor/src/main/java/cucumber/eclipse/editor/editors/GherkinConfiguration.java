package cucumber.eclipse.editor.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class GherkinConfiguration extends TextSourceViewerConfiguration {

	private GherkinKeywordScanner keywordScanner;
	private ColorManager colorManager;
	private Editor editor;

	public GherkinConfiguration(Editor editor, ColorManager colorManager) {
		this.editor = editor;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getReconciler
	 * (org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {

		IReconcilingStrategy strategy = new GherkinReconcilingStrategy(editor);

		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		return reconciler;
	}

	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 2;
	}
}