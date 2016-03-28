/**
 * 
 */
package cucumber.eclipse.editor.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.jdt.StepDefinitions;


/**
 * @author girija.panda@nokia.com
 * 
 * Purpose: Supports Content-Assistance of any Feature File for <Ctrl>+<Space> key
 * Content-Assistance : Keyword-Assistance and Step-Assistance based on user input
 * 1. Collect all steps from step-definition-java file
 * 2. Remove junk-words from Keyword List
 * 3. Populating of Keywords/Steps based on various RegEx pattern
 */
public class CucumberContentAssist {

	//Junk words needs to be filter form keyword list 
	private String[] junkWords = {"* ", "Business Need", "Ability", "Scenario Template", "Scenarios"};
	
	//Keywords used for Step
	public String[] stepKeywords = {"Given","When","Then","And","But"};
	
	//RegEx1 : (Line starts with only a <Step-keyword>[without <space> or <word>]) OR
	// (Line doesn't starts with a <Step-keyword>)
	public final String  KEYWORD_REGEX= "(^Given|When|Then|And|But|$[\\S\\W\\D])|(^(?!Given|When|Then|And|But).+)"; //issue
	
	//RegEx2 : Line starts with <Step-Keyword>+<space>+<any-word>
	public final String KEYWORD_SPACE_WORD_REGEX = "^(Given|When|Then|And|But)[\\s][\\w\\d\\s\\.]*";
	
	//RegEx-3 : starts with space
	public final String STARTSWITH_ANYSPACE = "^\\s+";
	
	//Regex-4 : Ends with any Space
	public final String ENDSWITH_ANYSPACE = "(\\s$)+";
	
	//Regex-5 : Word starts with any Space
	public  final String WORDS_STARTSWITH_SPACE = "(^\\s)[\\w]+";
	
	private final String NOSTEPS = "No Step Definition Found";
	
	//To collect Steps
	private List<String> stepDetailList = null;
	private List<String> matchedStepList = null;	
	private CompletionProposal allStepsProposal = null;
	private CompletionProposal noProposal = null;
	CompletionProposal matchedStepsProposal = null;	
	private Set<Step> importedSteps = null;	
	
	//Initialize 
	public CucumberContentAssist() {		
		
		//Collection of all steps
		importedSteps = StepDefinitions.steps;
		
		//Step lists
		stepDetailList = new ArrayList<String>();
		matchedStepList = new ArrayList<String>();		
	}

	
	// Iterate and collect step info
	public void collectAllSteps(String matchPrefix) {
		
		/* Commented for Performance
		 StepDetailList = new ArrayList<String>();
		 matchedStepList = new ArrayList<String>();		
		//Collection of all steps
		importedSteps = StepDefinitions.steps;*/
		
		if(!importedSteps.isEmpty()) 
		{
			// Collect all steps
			for(Step step : importedSteps) 
			{
				String stepText = getStepName(step.getText());
				String stepSource = getSourceName(step.getSource().toString());
				String lineNumber = step.getLineNumber() + "";
				String stepDetailInfo = stepText + " : " + stepSource + ":" + lineNumber;	
				// System.out.println("StepDetailInfo :" +stepDetailInfo);
				// System.out.println("*******************************************************************");
				
				//Collect only matched steps
				if(stepText.startsWith(matchPrefix) && 
					(!matchPrefix.matches(ENDSWITH_ANYSPACE)))
				{
					matchedStepList.add(stepDetailInfo);
				}
				
				//Collect all steps		
				stepDetailList.add(stepDetailInfo);
			}
		}
	
	}
	
	
	//Populate all step proposals
	public void importStepProposals(String prefix, int offset, Image ICON, String stepDetail, ArrayList<ICompletionProposal> result) {
		//Split the DetailStep
		String[] token = stepDetail.split(":");		
		String stepText = token[0].trim();
		String stepSource = token[1].trim();
		String lineNumber = token[2].trim();
		String stepWithSource = stepText + " : "+ stepSource;
		String stepDetails = "Step : " + stepText
							+ "\nSource : " + stepSource
							+ "\nLine Number : " + lineNumber;
		
		//System.out.println("ALL_STEP_DETAILS ="+stepDetails);		
		allStepsProposal = new CompletionProposal(stepText.replace(prefix, ""), offset, 0, stepText.length(),ICON, stepWithSource, null, stepDetails);
		result.add(allStepsProposal);
	}
	
	
	//No Proposals
	public void displayNoProposal(int offset, Image ICON, ArrayList<ICompletionProposal> result){		
		noProposal = new CompletionProposal("", offset, 0, 0, ICON, NOSTEPS, null, null);
		result.add(noProposal);
	}
	
	
	//Collect step as String
	public List<String> importAllStepList() {	
		return this.stepDetailList;
	}
	
	//Collect Matched Steps
	public List<String> importMatchedStepList() {	
		return this.matchedStepList;
	}
	


	//Get exact step
	public static String getStepName(String myStep){
		myStep = myStep.replaceAll("\\^|\\$", "");
		return myStep;
	}

	//Get Source File
	public static String getSourceName(String mySource){
		mySource = mySource.substring(mySource.lastIndexOf("/")).replace("/","");
		return mySource;
	}
	
	// Remove junk words from keyword list
	public void removeJunkWords(List<String> keywords){	
		List<String> junks = Arrays.asList(junkWords);
		keywords.removeAll(junks);
	}
	
	//Get all junkList
	public List<String> getJunkList(){
		List<String> junkList = Arrays.asList(junkWords);
		return junkList;
	}
	
	//Get First word of a String
	public String firstWord(String string) {
		String fistString = null;
		if (string.contains(" ")) {
			int index = string.indexOf(" ");
			fistString = string.substring(0, index);
			return fistString;
		} else
			return string;
	}

	//Get Last word of a String
	public String lastPrefix(String string) {
		String lastWord = null;
		if( string.contains(" ")|
		    string.endsWith(" ") )
		{								
			lastWord = string.substring(string.lastIndexOf(' '), string.length());							
			lastWord = lastWord.matches("(^\\s)[\\w]+")?lastWord=lastWord.trim():lastWord;
			return lastWord;
		}
		else
			return string;
	}


	/*
	 * private String lastWord(IDocument doc, int offset) { try { for (int n =
	 * offset-1; n >= 0; n--) { char c = doc.getChar(n); if
	 * (!Character.isJavaIdentifierPart(c)) return doc.get(n + 1, offset-n-1); }
	 * } catch (BadLocationException e) { // ... log the exception ... } return
	 * ""; }
	 * 
	 * private String lastIndent(IDocument doc, int offset) { try { int start =
	 * offset-1; while (start >= 0 && doc.getChar(start)!= '\n') start--; int
	 * end = start; while (end < offset &&
	 * Character.isSpaceChar(doc.getChar(end))) end++; return doc.get(start+1,
	 * end-start-1); } catch (BadLocationException e) { e.printStackTrace(); }
	 * return ""; }
	 */
	
}
