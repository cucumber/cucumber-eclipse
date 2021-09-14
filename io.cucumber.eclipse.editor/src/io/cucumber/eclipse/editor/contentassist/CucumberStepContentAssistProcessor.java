package io.cucumber.eclipse.editor.contentassist;

import java.util.Locale;

import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.steps.ExpressionDefinition;

/**
 * Provides content assists for cucumber steps
 * 
 * @author christoph
 *
 */
public class CucumberStepContentAssistProcessor implements IContentAssistProcessor {
	private static final int PREFIX_MATCH = Integer.MAX_VALUE / 2;
	private EditDistance<Integer> distance = new LongestCommonSubsequenceDistance();

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		return CucumberTemplates.computeTemplateProposals(viewer, offset, proposal -> {
			String prefix = proposal.getLinePrefix();
			ExpressionDefinition definition = proposal.getStepDefinition().getExpression();
			if (definition.getText().startsWith(prefix)) {
				proposal.setRelevance(PREFIX_MATCH);
			} else if (definition.matchIgnoreTypes(proposal.getLineText(), GherkinEditorDocument.get(viewer.getDocument()).getLocale())) {
				proposal.setRelevance(Integer.MAX_VALUE);
			} else {
				// TODO configure disable
				String lineText = proposal.getLineText();
				String expressionText = proposal.getStepDefinition().getExpression().getTextWithoutVariables();
				int d = distance.apply(lineText, expressionText);
				proposal.setRelevance(PREFIX_MATCH - d);
			}
			return true;
		});
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}


}
