package io.cucumber.eclipse.editor.syntaxhighlight;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

/**
 * Presentation reconciler for the gherking content type
 * 
 * @author christoph
 *
 */
public class GherkinPresentationReconciler extends PresentationReconciler implements IPresentationReconciler {

	public GherkinPresentationReconciler() {
		DefaultDamagerRepairer dr = new GherkinDamagerRepairer(new GherkinKeywordScanner());
		this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

}
