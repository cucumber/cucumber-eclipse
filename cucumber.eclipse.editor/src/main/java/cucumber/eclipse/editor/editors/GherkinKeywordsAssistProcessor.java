package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.graphics.Image;

import cucumber.eclipse.editor.Activator;
import gherkin.I18n;

public class GherkinKeywordsAssistProcessor implements IContentAssistProcessor {
	//TODO This line copy from i18n,because of private var.
	private static final List<String> FEATURE_ELEMENT_KEYWORD_KEYS = Arrays.asList("feature", "background", "scenario", "scenario_outline", "examples");
	
    private final IContextInformation[] NO_CONTEXTS = {};
    private ICompletionProposal[] NO_COMPLETIONS = {};

    public final Image ICON = Activator.getImageDescriptor("icons/cukes.gif").createImage();

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
            int offset) {
        try {
            // TODO listen some language changed event.
            IDocument document = viewer.getDocument();

            // TODO a service bean.
            String lang = DocumentUtil.getDocumentLanguage(document);
            if (lang == null) {
                lang = "en";
            }
            I18n i18n = new I18n(lang);

            List<String> stepKeywords = i18n.getStepKeywords();

            // line of cursor locate,and from begin to cursor.
            IRegion line = viewer.getDocument()
                    .getLineInformationOfOffset(offset);
            String typed = viewer.getDocument()
                    .get(line.getOffset(), offset - line.getOffset())
                    .replaceAll("^\\s+", "");
            ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

            if (typed.length() > 0) {
                // all key words
                List<String> keywords = allKeywords(i18n);
                for (String string : keywords) {
                    if (string.startsWith(typed)) {
                        CompletionProposal p = new CompletionProposal(string,
                                offset - typed.length(), typed.length(),
                                string.length(), ICON, null, null, null);
                        result.add(p);
                    }
                }
            } else {
                List<String> keywords = allKeywords(i18n);
                for (String string : keywords) {
                    CompletionProposal p = new CompletionProposal(string,
                            offset, 0, string.length(), ICON, null, null, null);
                    result.add(p);
                }
            }

            return (ICompletionProposal[]) result
                    .toArray(new ICompletionProposal[result.size()]);
        } catch (Exception e) {
            // ... log the exception ...
            return NO_COMPLETIONS;
        }
    }

    private List<String> allKeywords(I18n i18n) {
        List<String> keywords = i18n.getStepKeywords();
        for (String string : FEATURE_ELEMENT_KEYWORD_KEYS) {
        	keywords.addAll(i18n.keywords(string));
        }
        return keywords;
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer,
            int offset) {
        return NO_CONTEXTS;
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
