package io.cucumber.eclipse.editor.contentassist;

import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.editor.steps.ExpressionDefinition;

/**
 * provides assistance for "missing" steps that are probably only have wrong
 * parameter
 * 
 * @author christoph
 *
 */
public class CucumberStepQuickAssistProcessor implements IQuickAssistProcessor {

	private EditDistance<Double> distance = new JaroWinklerDistance();

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean canFix(Annotation annotation) {
		return annotation instanceof MarkerAnnotation;
	}

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return GherkinEditorDocumentManager.isCompatibleTextBuffer(invocationContext.getSourceViewer().getDocument());
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		ISourceViewer viewer = invocationContext.getSourceViewer();
		IDocument document = viewer.getDocument();
		try {
			int line = document.getLineOfOffset(invocationContext.getOffset());
			GherkinEditorDocument editorDocument = GherkinEditorDocumentManager.get(document);
			if (MarkerFactory.hasMarker(editorDocument.getResource(), MarkerFactory.UNMATCHED_STEP, line + 1)) {
				return CucumberTemplates.computeTemplateProposals(viewer, invocationContext.getOffset(), (proposal) -> {
					String lineText = proposal.getLineText();
					ExpressionDefinition expression = proposal.getStepDefinition().getExpression();
					if (expression.matchIgnoreTypes(lineText, editorDocument.getLocale())) {
						proposal.setRelevance(Integer.MAX_VALUE);
						return true;
					}
					// TODO configure to disable this
					String expressionText = expression.getTextWithoutVariables();
					double sim = distance.apply(lineText, expressionText);
					proposal.setRelevance((int) (100 * sim));
					// TODO configurable
					return sim > 0.6;
				});
			}
		} catch (BadLocationException e) {
		} catch (CoreException e) {
		}
		return null;
	}

}
