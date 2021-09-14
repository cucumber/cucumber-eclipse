package io.cucumber.eclipse.editor.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.Images;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinKeyword;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;

/**
 * Provides content assist for gherkin keywords
 * 
 * @author christoph
 *
 */
public class GherkinContentAssistProcessor implements IContentAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		IDocument document = viewer.getDocument();
		GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
		try {
			IRegion line = viewer.getDocument().getLineInformationOfOffset(offset);
			String typed = viewer.getDocument().get(line.getOffset(), offset - line.getOffset()).stripLeading();
			String nl = TextUtilities.getDefaultLineDelimiter(document);
			if (typed.isBlank()) {
				// TODO if the line is not blank, we might try to assume that the document is
				// only invalid because the user is typing, we could try to use the latest valid
				// document to analyze its structure...
				Optional<Feature> feature = editorDocument.getFeature();
				if (feature.isEmpty()) {
					// if the document do not contain a feature this is the only choice
					return editorDocument.getFeatureKeywords().filter(keyWord -> keyWord.prefix(typed))
							.map(featureKeyWord -> createFeatureKeyWordProposal(featureKeyWord.getKey(), offset, typed))
							.toArray(ICompletionProposal[]::new);
				}
				Predicate<FeatureChild> hasTopLevelElement = FeatureChild::hasBackground;
				hasTopLevelElement = hasTopLevelElement.or(FeatureChild::hasRule);
				hasTopLevelElement = hasTopLevelElement.or(FeatureChild::hasScenario);
				if (editorDocument.getFeatureChilds().filter(hasTopLevelElement).count() == 0) {
					// in this case Rule, Scenario or background are required
					return editorDocument.getTopLevelKeywords()//
							.filter(keyWord -> keyWord.prefix(typed)).sorted(GherkinKeyword.KEY_ORDER) //
							.map(keyWord -> createCompletionProposal(offset, typed, keyWord.getKey(),
									keyWord.getKey() + ":" + nl))
							.toArray(ICompletionProposal[]::new);
				}
			}

			// TODO other constrains, filtering etc...
			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			editorDocument.getTopLevelKeywords().filter(keyWord -> keyWord.prefix(typed))
					.sorted(GherkinKeyword.KEY_ORDER)
					.map(keyWord -> createCompletionProposal(offset, typed, keyWord.getKey(),
							keyWord.getKey() + ":" + nl))
					.forEach(result::add);

			editorDocument.getStepElementKeywords().filter(keyWord -> keyWord.prefix(typed))
					.sorted(GherkinKeyword.KEY_ORDER)
					.map(keyWord -> createCompletionProposal(offset, typed, keyWord.getKey(), keyWord.getKey() + " "))
					.forEach(result::add);
			// TODO datatables, docstrings, ...
			return result.toArray(ICompletionProposal[]::new);
		} catch (BadLocationException e) {
			Activator.getDefault().getLog().warn("Invalid location encountered while computing proposals", e);
		}
		return null;

	}

	private CompletionProposal createCompletionProposal(int offset, String typed, String displayString,
			String fullString) {
		return new CompletionProposal(fullString, offset - typed.length(), typed.length(), fullString.length(),
				Images.getCukesIcon(), displayString, null, null);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// TODO provide context infos according to cucumber documentation see
		// https://cucumber.io/docs/gherkin/reference/
		IDocument document = viewer.getDocument();
		GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
		try {
			IRegion line = viewer.getDocument().getLineInformationOfOffset(offset);

			String typed = viewer.getDocument().get(line.getOffset(), offset - line.getOffset()).stripLeading();

			editorDocument.keyWords(GherkinDialect::getFeatureKeywords);

		} catch (BadLocationException e) {
			Activator.getDefault().getLog().warn("Invalid location encountered while computing context information", e);
		}
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO see bug 508821
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		return str.toCharArray();
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
		System.out.println("getContextInformationValidator()");
		// return new ContextInformationValidator(this);
		return new IContextInformationValidator() {

			@Override
			public boolean isContextInformationValid(int offset) {
				System.out.println("isContextInformationValid()");
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public void install(IContextInformation info, ITextViewer viewer, int offset) {
				// TODO Auto-generated method stub
				System.out.println("install()");

			}
		};
	}

	private static CompletionProposal createFeatureKeyWordProposal(String keyWord, int offset, String typed) {
		String fullString = keyWord + ": ";
		return new CompletionProposal(fullString, offset - typed.length(), typed.length(), fullString.length(),
				Images.getCukesIcon(), keyWord,
				new ContextInformation("Name of Feature", "Enter the Name of the Feature under test"),
				"Start a new feature");
	}

}
