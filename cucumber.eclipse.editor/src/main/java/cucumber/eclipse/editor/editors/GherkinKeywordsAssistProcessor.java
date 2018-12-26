

package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.Arrays;
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
import cucumber.eclipse.editor.steps.UniversalStepDefinitionsProvider;
import gherkin.I18n;

public class GherkinKeywordsAssistProcessor implements IContentAssistProcessor {
	
	// TODO This line copy from i18n,because of private var.
	private static final List<String> FEATURE_ELEMENT_KEYWORD_KEYS = Arrays.asList("feature", "background", "scenario", "scenario_outline","examples");
	private final IContextInformation[] NO_CONTEXTS = {};
	private ICompletionProposal[] NO_COMPLETIONS = {};
	public final Image ICON = Activator.getImageDescriptor("icons/cukes.gif").createImage();
	
	//For content assistance feature By Girija
	private CucumberContentAssist contentAssist = null;
	private Editor editor;
	
	public GherkinKeywordsAssistProcessor(Editor editor) {
		this.editor = editor;
	}

	// To Compute CompletionProposals
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			
			
			// TODO listen some language changed event.
			IDocument document = viewer.getDocument();

			// TODO a service bean.
			String lang = DocumentUtil.getDocumentLanguage(document);

			if (lang == null) {
				lang = "en";
			}
			IProject project = this.editor.getFile().getProject();
			I18n i18n = new I18n(lang);

			//Initialize CucumberContentAssist
			this.contentAssist = new CucumberContentAssist(lang, project);
			List<String> stepKeyWords = contentAssist.getStepKeyWords(i18n);
			// line of cursor locate,and from begin to cursor.
			IRegion line = viewer.getDocument().getLineInformationOfOffset(offset);
			
			// Capture User Typed String with trimmed white-space from the start of typed string
			String typed = viewer.getDocument().get(line.getOffset(), offset - line.getOffset()).replaceAll(contentAssist.STARTSWITH_ANYSPACE, "");
			//System.out.println("USER-TYPED =" + typed);

			// Capture any Existing Step
			String preStep = viewer.getDocument().get(line.getOffset(), line.getLength()).replaceAll(contentAssist.STARTSWITH_ANYSPACE, "");
			preStep = this.contentAssist.lastPrefix(preStep,stepKeyWords);
			//System.out.println("PRE-STEP =" + preStep);
			
			// Create an ArrayList instance for collecting the generated
			// ICompletionProposal instance
			ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			
			// Fetch All key words and remove Junk words
			List<String> keywordList = allKeywords(i18n); 
			List<String> junks = this.contentAssist.getJunkList();
			// System.out.println("junks :" +junks);
			
			// Remove junk words from Keyword-List
			if (keywordList.containsAll(junks))
				keywordList.removeAll(junks);
		
			//To display proposals for any char/string is typed
			if (typed.length() > 0) 
			{
				
				for (String stepKeyWord : stepKeyWords) {
					
					//1. Check if typed is the prefix of any stepword (ignoring trailing space)
					if (stepKeyWord.trim().startsWith(typed)) {
						addStepWordCompletionProposal(offset, typed, result, stepKeyWord);
					}
					
					//2.To Check Typed = <Keyword any-words>
					else if (typed.startsWith(stepKeyWord)){
						addStepDetailsProposal(offset, typed, preStep, result, stepKeyWords);
					}
				}
			}
			//New Line starts with blank
			else 
			{
				// Don't Delete : Used For Debug
				//System.out.println("ELSE.... ");
				// Populate all Keywords
				for (String string : keywordList) {
					CompletionProposal p = new CompletionProposal(string, offset, 0, string.length(), ICON, null, null, null);
					result.add(p);
				}
			}

			return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);
		} catch (Exception e) {
			// ... log the exception ...
			return NO_COMPLETIONS;
		}
	}

	private void addStepDetailsProposal(int offset, String typed, String preStep,
			ArrayList<ICompletionProposal> result, List<String> stepKeyWords) {
		//System.out.println("IF-2:Inside ...");

		String lastPrefix = contentAssist.lastPrefix(typed,stepKeyWords);
		//System.out.println("LAST-PREFIX = " +lastPrefix);
		
		//Collect all steps with 
		contentAssist.collectAllSteps(lastPrefix);
		
		// Collect all steps and matched steps from step-definition file
		List<String> stepDetailList = contentAssist.importAllStepList();
		List<String> matchedStepList = contentAssist.importMatchedStepList();
		
		 // Don't Delete : Used For Debug
		//System.out.println("stepDetailList = " +stepDetailList);
		//System.out.println("matchedStepList = " +matchedStepList);					
		
		//1. NO-Proposal For Any Empty List
		if( matchedStepList.isEmpty() &&
				stepDetailList.isEmpty())
		{
			 // Don't Delete : Used For Debug
			//System.out.println("NO PROPOSAL-1***************");
			contentAssist.displayNoProposal(offset, ICON, result);
		}
		
		//2. Proposal For Non-Empty Matched-Step-List
		else if(!matchedStepList.isEmpty())
		{
			 // Don't Delete : Used For Debug
			//System.out.println("matchedStepList NOT EMPTY......");
			for(String step : matchedStepList)
			{							
				//For starts-with Step proposal 
				if(step.startsWith(lastPrefix))
				{	
					//Populate all matched starts-with steps
					contentAssist.importStepProposals(lastPrefix, preStep, offset, ICON, step, result);							
				}
				
				//For contains Step proposal
				else if(step.contains(lastPrefix))
				{							 
					//Populate all matched contains steps
					contentAssist.importStepProposals(lastPrefix, preStep, offset, ICON, step, result);							
				}
			}
		}
		
		//3. No proposal for Empty Matched-Step-List
		else if( lastPrefix.matches(contentAssist.STARTS_ANY) && 
				 matchedStepList.isEmpty() )
		{
			 // Don't Delete : Used For Debug
			//System.out.println("NO PROPOSAL-2***************");
			contentAssist.displayNoProposal(offset, ICON, result);
		}
		
		//4. All Step-Proposals if any <space> and COMMA
		else if(lastPrefix.matches(contentAssist.COMMA_SPACE_REGEX) |
				!stepDetailList.isEmpty())
		{
			 // Don't Delete : Used For Debug
			//System.out.println("stepDetailList NOT EMPTY......");
			for(String step : stepDetailList)
			{	
				 // Don't Delete : Used For Debug
				//System.out.println("stepDetailList ###########");
				//Populate all step proposal
				contentAssist.importStepProposals(lastPrefix, preStep, offset, ICON, step, result);
			}
		}

		//5. No Match No Proposal
		else
		{
			 // Don't Delete : Used For Debug
			//System.out.println("NO PROPOSAL-3***************");
			contentAssist.displayNoProposal(offset, ICON, result);
		}
	}

	private void addStepWordCompletionProposal(int offset, String typed, ArrayList<ICompletionProposal> result,
			String stepKeyWord) {
		CompletionProposal p = new CompletionProposal(stepKeyWord, offset - typed.length(), typed.length(),stepKeyWord.length(), ICON, null, null, null);
		result.add(p);
	}

	
	// To get all keywords
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
		//return PROPOSAL_ACTIVATION_CHARS;
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
