
package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.contentassist.CucumberContentAssist;
import cucumber.eclipse.steps.integration.KeyWordProvider;

public class GherkinKeywordsAssistProcessor implements IContentAssistProcessor {

	private final IContextInformation[] NO_CONTEXTS = {};
	private ICompletionProposal[] NO_COMPLETIONS = {};
	public final Image ICON = Activator.getImageDescriptor("icons/cukes.gif").createImage();

	private Editor editor;
	private String errorMsg;

	public GherkinKeywordsAssistProcessor(Editor editor) {
		this.editor = editor;
	}

	// To Compute CompletionProposals
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		errorMsg = null;
		try {

			// TODO listen some language changed event.
			IDocument document = viewer.getDocument();

			// TODO a service bean.
			String lang = DocumentUtil.getDocumentLanguage(document);

			if (lang == null) {
				lang = "en";
			}
			IProject project = this.editor.getFile().getProject();

			// Initialize CucumberContentAssist
			CucumberContentAssist contentAssist = new CucumberContentAssist(lang, project);

			KeyWordProvider keyWordProvider = getKeyWordProvider(project);
			if (keyWordProvider == null) {
				errorMsg = "Can't fetch keywords!";
				return NO_COMPLETIONS;
			}

			List<String> stepKeyWords = keyWordProvider.getStepKeyWords(lang);
			List<String> groupingKeyWords = keyWordProvider.getGroupingKeyWords(lang);
			// line of cursor locate,and from begin to cursor.
			IRegion line = viewer.getDocument().getLineInformationOfOffset(offset);

			// Capture User Typed String with trimmed white-space from the start of typed
			// string
			String typed = viewer.getDocument().get(line.getOffset(), offset - line.getOffset())
					.replaceAll(contentAssist.STARTSWITH_ANYSPACE, "");
			// System.out.println("USER-TYPED =" + typed);

			// Capture any Existing Step
			String preStep = viewer.getDocument().get(line.getOffset(), line.getLength())
					.replaceAll(contentAssist.STARTSWITH_ANYSPACE, "");
			preStep = contentAssist.lastPrefix(preStep, stepKeyWords);
			// System.out.println("PRE-STEP =" + preStep);

			// Create an ArrayList instance for collecting the generated
			// ICompletionProposal instance
			ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

			for (String groupingKeyWord : groupingKeyWords) {
				if (groupingKeyWord.startsWith(typed)) {
					result.add(createCompletionProposal(offset, typed, groupingKeyWord, groupingKeyWord + ":\r\n"));
				}
			}

			for (String stepKeyWord : stepKeyWords) {

				// 1. Check if typed is the prefix of any stepword (ignoring trailing space)
				if (stepKeyWord.startsWith(typed)) {
					result.add(createCompletionProposal(offset, typed, stepKeyWord, stepKeyWord + " "));
				}

				// 2.To Check Typed = <Keyword any-words>
				else if (typed.startsWith(stepKeyWord)) {
					addStepDetailsProposal(offset, typed, preStep, result, stepKeyWords,contentAssist);
				}
			}

			return result.toArray(new ICompletionProposal[result.size()]);
		} catch (Exception e) {
			// ... log the exception ...
			e.printStackTrace();
			errorMsg = e.toString();
			return NO_COMPLETIONS;
		}
	}

	private KeyWordProvider getKeyWordProvider(IProject project) {
		@SuppressWarnings("restriction")
		Object adapter = org.eclipse.core.internal.runtime.AdapterManager.getDefault().loadAdapter(project,
				KeyWordProvider.class.getName());
		if (adapter instanceof KeyWordProvider) {
			return (KeyWordProvider) adapter;
		}
		return project.getAdapter(KeyWordProvider.class);
	}

	private void addStepDetailsProposal(int offset, String typed, String preStep, ArrayList<ICompletionProposal> result,
			List<String> stepKeyWords, CucumberContentAssist contentAssist ){
		// System.out.println("IF-2:Inside ...");

		String lastPrefix = contentAssist.lastPrefix(typed, stepKeyWords);

		// Collect all steps with
		contentAssist.collectAllSteps(lastPrefix);

		// Collect all steps and matched steps from step-definition file
		List<String> stepDetailList = contentAssist.importAllStepList();
		List<String> matchedStepList = contentAssist.importMatchedStepList();

		// Don't Delete : Used For Debug
		// System.out.println("stepDetailList = " +stepDetailList);
		// System.out.println("matchedStepList = " +matchedStepList);

		// 1. NO-Proposal For Any Empty List
		if (matchedStepList.isEmpty() && stepDetailList.isEmpty()) {
			// Don't Delete : Used For Debug
			// System.out.println("NO PROPOSAL-1***************");
			contentAssist.displayNoProposal(offset, ICON, result);
		}

		// 2. Proposal For Non-Empty Matched-Step-List
		else if (!matchedStepList.isEmpty()) {
			// Don't Delete : Used For Debug
			// System.out.println("matchedStepList NOT EMPTY......");
			for (String step : matchedStepList) {
				// For starts-with Step proposal
				if (step.startsWith(lastPrefix)) {
					// Populate all matched starts-with steps
					contentAssist.importStepProposals(lastPrefix, preStep, offset, ICON, step, result);
				}

				// For contains Step proposal
				else if (step.contains(lastPrefix)) {
					// Populate all matched contains steps
					contentAssist.importStepProposals(lastPrefix, preStep, offset, ICON, step, result);
				}
			}
		}

		// 3. No proposal for Empty Matched-Step-List
		else if (lastPrefix.matches(contentAssist.STARTS_ANY) && matchedStepList.isEmpty()) {
			// Don't Delete : Used For Debug
			// System.out.println("NO PROPOSAL-2***************");
			contentAssist.displayNoProposal(offset, ICON, result);
		}

		// 4. All Step-Proposals if any <space> and COMMA
		else if (lastPrefix.matches(contentAssist.COMMA_SPACE_REGEX) | !stepDetailList.isEmpty()) {
			// Don't Delete : Used For Debug
			// System.out.println("stepDetailList NOT EMPTY......");
			for (String step : stepDetailList) {
				// Don't Delete : Used For Debug
				// System.out.println("stepDetailList ###########");
				// Populate all step proposal
				contentAssist.importStepProposals(lastPrefix, preStep, offset, ICON, step, result);
			}
		}

		// 5. No Match No Proposal
		else {
			// Don't Delete : Used For Debug
			// System.out.println("NO PROPOSAL-3***************");
			contentAssist.displayNoProposal(offset, ICON, result);
		}
	}

	private CompletionProposal createCompletionProposal(int offset, String typed, String displayString,
			String fullString) {
		//TODO we should check if this is a completion inside a word e.g. An<cursor here>d currently produces And <cursor here>d
		return new CompletionProposal(fullString, offset - typed.length(), typed.length(),
				fullString.length(), ICON, displayString, null, null);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
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
		return errorMsg;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
