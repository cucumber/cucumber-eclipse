package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.graphics.Image;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.contentassist.CucumberContentAssist;
import cucumber.eclipse.steps.integration.Step;
import gherkin.I18n;

public class GherkinKeywordsAssistProcessor implements IContentAssistProcessor {
	
	// TODO This line copy from i18n,because of private var.
	private static final List<String> FEATURE_ELEMENT_KEYWORD_KEYS = Arrays.asList("feature", "background", "scenario", "scenario_outline","examples");
	private final IContextInformation[] NO_CONTEXTS = {};
	private ICompletionProposal[] NO_COMPLETIONS = {};
	public final Image ICON = Activator.getImageDescriptor("icons/cukes.gif").createImage();
	
	//For content assistance feature By Girija
	private CucumberContentAssist contentAssist = null;
	
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

			I18n i18n = new I18n(lang);

			//Initialize CucumberContentAssist
			contentAssist = new CucumberContentAssist();
			
			// line of cursor locate,and from begin to cursor.
			IRegion line = viewer.getDocument().getLineInformationOfOffset(offset);
			// To trim white-space from the start of typed string
			String typed = viewer.getDocument().get(line.getOffset(), offset - line.getOffset()).replaceAll(contentAssist.STARTSWITH_ANYSPACE, "");
			//System.out.println("USER-TYPED =" + typed);

			// Create an ArrayList instance for collecting the generated
			// ICompletionProposal instance
			ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			
		
			// Fetch All key words and remove Junk words
			List<String> keywordList = allKeywords(i18n); 
			List<String> junks = contentAssist.getJunkList();
			// System.out.println("junks :" +junks);
			
			// Remove junk words from Keyword-List
			if (keywordList.containsAll(junks))
				keywordList.removeAll(junks);
		
			//To display proposals for any char/string is typed
			if (typed.length() > 0) 
			{
				//1. Check Typed=<Keyword> OR Typed=AnyWord
				//POPULATE KEYWORD ASSISTANCE
				if (typed.matches(contentAssist.KEYWORD_REGEX)) 
				{
					//System.out.println("IF-1:Inside ...");
					// To display only Step-Keywords
					for (String string : contentAssist.stepKeywords) {
						CompletionProposal p = new CompletionProposal(string, offset - typed.length(), typed.length(),string.length(), ICON, null, null, null);
						result.add(p);
					}
				}

				//2.Check Typed = <Keyword any-words>
				//POPULATE STEP ASSISTANCE
				if (typed.matches(contentAssist.KEYWORD_SPACE_WORD_REGEX)) 
				{
					//System.out.println("IF-2:Inside ...");
					String lastPrefix = contentAssist.lastPrefix(typed);
					//System.out.println("LAST-PREFIX =" +lastPrefix);

					//Collect all steps with 
					contentAssist.collectAllSteps(lastPrefix);
					
					// Collect all steps and matched steps from step-definition file
					List<String> stepDetailList = contentAssist.importAllStepList();
					List<String> matchedStepList = contentAssist.importMatchedStepList();
					
					//System.out.println("stepDetailList = " +stepDetailList);
					//System.out.println("matchedStepList = " +matchedStepList);
					
					//1.
					if( matchedStepList.isEmpty() &&
							stepDetailList.isEmpty())
					{
						//System.out.println("NO PROPOSAL-1***************");
						contentAssist.displayNoProposal(offset, ICON, result);
					}
					
					//Proposal For Non-Empty Matched-Step-List
					else if(!matchedStepList.isEmpty())
					{
						//System.out.println("matchedStepList NOT EMPTY......");
						for(String step : matchedStepList)
						{							
							//Matched Step proposal
							if(step.startsWith(lastPrefix))
							{	
								//Populate all matched steps
								contentAssist.importStepProposals(lastPrefix, offset, ICON, step, result);							
							}
						}
					}
					
					//No proposal for Unmatched Prefix AND Empty Matched-Step-List
					else if(!lastPrefix.startsWith(" ") && 
							matchedStepList.isEmpty())
					{
						//System.out.println("NO PROPOSAL-2***************");
						contentAssist.displayNoProposal(offset, ICON, result);
					}
					
					
					
					
					//All Step-Proposals if any <space>
					else if(lastPrefix.startsWith(" ") | 
							!stepDetailList.isEmpty())
					{
						//System.out.println("stepDetailList NOT EMPTY......");
						for(String step : stepDetailList)
						{							
							//System.out.println("stepDetailList ###########");
							//Populate all step proposal
							contentAssist.importStepProposals(lastPrefix, offset, ICON, step, result);
						}
					}
					
					
					
					
					
					else
					{
						//System.out.println("NO PROPOSAL-3***************");
						contentAssist.displayNoProposal(offset, ICON, result);
					}
				}
			}
		
			
			//New Line starts with blank
			else 
			{
				//System.out.println("ELSE.... ");
				// Populate all Keywords
				for (String string : keywordList) {
					CompletionProposal p = new CompletionProposal(string, offset, 0, string.length(), ICON, null, null, null);
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
