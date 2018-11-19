package cucumber.eclipse.editor.editors;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import cucumber.eclipse.editor.markers.MarkerResolutionProposal;
import cucumber.eclipse.editor.markers.StepCreationMarkerResolutionGenerator;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants;

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

	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		IQuickAssistAssistant quickAssist = new QuickAssistAssistant();
		quickAssist.setQuickAssistProcessor(new IQuickAssistProcessor() {

			@Override
			public String getErrorMessage() {
				return null;
			}

			@Override
			public boolean canFix(Annotation annotation) {
				return true;
			}

			@Override
			public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
				return false;
			}

			@Override
			public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
				IAnnotationModel annotationModel = invocationContext.getSourceViewer().getAnnotationModel();
				
				for (Iterator<?> i = annotationModel.getAnnotationIterator(); i.hasNext(); ) {
					Annotation testAnnotation = (Annotation) i.next();
					if (annotationModel.getPosition(testAnnotation).includes(editor.getSelection().getOffset())) {
						if (testAnnotation instanceof MarkerAnnotation) {
							IMarker marker = ((MarkerAnnotation) testAnnotation).getMarker();
							IMarkerResolution[] markerResolutions = new StepCreationMarkerResolutionGenerator()
								.getResolutions(marker);
							
							ICompletionProposal[] completionProposals = new ICompletionProposal[markerResolutions.length];
							for (int j = 0; j < markerResolutions.length; j ++) {
								completionProposals[j] = new MarkerResolutionProposal(marker, markerResolutions[j]);
							}
							
							return completionProposals;
						}
					}
				}
				
				return null;
			}
		});
		quickAssist.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return quickAssist;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		DefaultDamagerRepairer dr = new GherkinDamagerRepairer(getGherkinKeywordScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		if (store.getBoolean(ICucumberPreferenceConstants.PREF_CHECK_STEP_DEFINITIONS)) {
			ContentAssistant ca = new ContentAssistant();
			IContentAssistProcessor cap = new GherkinKeywordsAssistProcessor(editor);
				ca.setContentAssistProcessor(cap, IDocument.DEFAULT_CONTENT_TYPE);
				ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
				
				//Added By Girija For Content Assistance
				ca.setStatusMessage("<Step>:<Class>, Press 'Ctrl+SPace' For Cucumber Assistance");
				ca.setStatusLineVisible(true);
				
				ca.enableAutoActivation(true);
				ca.setAutoActivationDelay(500);
				//ca.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				//ca.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
				
			return ca;
		}
		return null;
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
	
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		IHyperlinkDetector[] hyperlinkDetectors = super.getHyperlinkDetectors(sourceViewer);
		StepHyperlinkDetector stepHyperlinkDetector = new StepHyperlinkDetector(this.editor);
		IHyperlinkDetector[] gherkinHyperlinkDetectors = Arrays.copyOf(hyperlinkDetectors, hyperlinkDetectors.length + 1);
		gherkinHyperlinkDetectors[hyperlinkDetectors.length] = stepHyperlinkDetector;
		return gherkinHyperlinkDetectors;
	}
}