/**
 * 
 */
package cucumber.eclipse.editor.contentassist;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

import cucumber.eclipse.editor.steps.IStepProvider;
import cucumber.eclipse.steps.integration.Step;
import gherkin.I18n;

/**
 * @author girija.panda@nokia.com
 * 
 *         Purpose: Supports Content-Assistance of any Feature File for
 *         <Ctrl>+<Space> key Content-Assistance : Keyword-Assistance and
 *         Step-Assistance based on user input 1. Collect all steps from
 *         step-definition-java file 2. Remove junk-words from Keyword List 3.
 *         Populating of Keywords/Steps based on various RegEx pattern
 */
public class CucumberContentAssist {

	private final String NOSTEPS = "No Step Definition Found";

	// Junk words needs to be filter form keyword list
	private String[] junkWords = { "* ", "Business Need", "Ability", "Scenario Template", "Scenarios" };

	// RegEx1 : (Line starts with only a <Step-keyword>[without <space> or
	// <word>]) OR
	// (Line doesn't starts with a <Step-keyword>)
	public final String KEYWORD_REGEX = "(^Angenommen|Wenn|Dann|Und|Aber|$[\\S\\W\\D])|(^(?!Angenommen|Wenn|Dann|Und|Aber).+)"; // issue

	// RegEx2 : Line starts with <Step-Keyword>+<space>+<any-word>
	public final String KEYWORD_SPACE_WORD_REGEX = "^(Angenommen|Wenn|Dann|Und|Aber)[\\s][\\w\\d\\s\\S]*";

	// RegEx-3 : starts with space
	public final String STARTSWITH_ANYSPACE = "^\\s+";

	// Regex-4 : Ends with any Space
	public final String ENDSWITH_ANYSPACE = "(\\s$)+";

	// Regex-5 : Word starts with any Space
	public final String WORDS_STARTSWITH_SPACE = "(^\\s)[\\w]+";

	// Regex-6 : Word starts with any Space
	// public final String STARTS_ANY_WORD_DIGIT_SPACE= "^[\\w\\d\\s]+";

	// Regex-7 : Word starts with any Word/Digit/NonWord/Space
	public final String STARTS_ANY = "^[\\w\\d\\S][^\\,]*";

	// Regex-8 : Word starts with any Word/Digit/NonWord/Space
	public final String KEYWORD_SPACE_REGEX = "^(Angenommen|Wenn|Dann|Und|Aber)\\s+";
	
	public final String COMMA_SPACE_REGEX = "(\\,|\\s$)+";

	// Integer
	public final String INTEGER = "(\\d+)";
	// Real Number
	public final String REAL_NUMBER = "(\\d+\\.\\d+)";
	// Text
	public final String TEXT = "([\"]*)";
	// Any Text
	public final String ANY_TEXT = "(.*?)";
	// Start Or End
	public final static String START_OR_END = "\\^|\\$";

	// To collect Steps
	private List<String> stepDetailList = null;
	private List<String> matchedStepList = null;
	private CompletionProposal allStepsProposal = null;
	private CompletionProposal noProposal = null;
	CompletionProposal matchedStepsProposal = null;
	private Set<Step> importedSteps = null;

	private String lang;

	// Initialize
	public CucumberContentAssist(String lang, IStepProvider stepProvider) {
		this.lang = lang;
		importedSteps = stepProvider.getStepsInEncompassingProject();

		// Step lists
		stepDetailList = new ArrayList<String>();
		matchedStepList = new ArrayList<String>();
	}

	public List<String> getStepKeyWords() {
		List<String> stepKeywords = new I18n(lang).getStepKeywords();
		stepKeywords.removeAll(asList(junkWords));
		return stepKeywords;
	}

	// Iterate and collect step info
	public void collectAllSteps(String matchPrefix) {

		/*
		 * Commented for Performance StepDetailList = new ArrayList<String>();
		 * matchedStepList = new ArrayList<String>(); //Collection of all steps
		 * importedSteps = StepDefinitions.steps;
		 */

		if (!importedSteps.isEmpty()) {
			// Collect all steps
			for (Step step : importedSteps) {
				String stepText = getStepName(step.getText());
				String stepSource = getSourceName(step.getSource().toString());
				String lineNumber = step.getLineNumber() + "";
				String stepDetailInfo = stepText + " : " + stepSource + ":"
						+ lineNumber;
				// System.out.println("StepDetailInfo :" +stepDetailInfo);
				// System.out.println("*******************************************************************");

				// Collect only matched steps
				/*
				 * if(stepText.startsWith(matchPrefix) &&
				 * (!matchPrefix.matches(ENDSWITH_ANYSPACE))) {
				 * matchedStepList.add(stepDetailInfo); }
				 */
				if (stepText.startsWith(matchPrefix)
						&& matchPrefix.matches(STARTS_ANY)) 
				{
					matchedStepList.add(stepDetailInfo);
				}

				// Collect all steps
				stepDetailList.add(stepDetailInfo);
			}
		}

	}

	// Populate all step proposals
	public void importStepProposals(String prefix, String preStep, int offset,
			Image ICON, String stepDetail, ArrayList<ICompletionProposal> result) {

		// Split the DetailStep
		String stepWithSource = stepDetail.substring(0,stepDetail.lastIndexOf(":")).trim();
		String stepText = stepWithSource.substring(0,stepWithSource.lastIndexOf(":")).trim();
		String stepSource = stepWithSource.substring(stepWithSource.lastIndexOf(":") + 1).trim();
		String lineNumber = stepDetail.substring(stepDetail.lastIndexOf(":") + 1).trim();

		String stepDetails = "Step : " + stepText 
							+ "\nSource : " + stepSource
							+ "\nLine Number : " + lineNumber;

		// System.out.println("ALL_STEP_DETAILS ="+stepDetails);
		// System.out.println("PREFIX ="+prefix);
		// if(!prefix.startsWith(" ")) {
		
		if (preStep.length() > prefix.length()) 
		{
			//System.out.println("IF : PREFIX =" + prefix);
			//System.out.println("IF : PRESTEP =" + preStep);
			//System.out.println("IF : OFFSET =" + offset);
			allStepsProposal = new CompletionProposal(getUserStep(stepText), offset - prefix.length(), preStep.length(), stepText.length(), ICON, stepWithSource, null, stepDetails);
		} 
		else 
		{
			//System.out.println("IF ELSE : PREFIX =" + prefix);
			//System.out.println("IF ELSE : PRESTEP =" + preStep);
			allStepsProposal = new CompletionProposal(getUserStep(stepText).replace(prefix, ""), offset, 0, stepText.length(), ICON, stepWithSource, null, stepDetails);
		}

		result.add(allStepsProposal);
	}

	// No Proposals
	public void displayNoProposal(int offset, Image ICON, ArrayList<ICompletionProposal> result) {
		noProposal = new CompletionProposal("", offset, 0, 0, ICON, NOSTEPS, null, null);
		result.add(noProposal);
	}

	// Collect step as String
	public List<String> importAllStepList() {
		return this.stepDetailList;
	}

	// Collect Matched Steps
	public List<String> importMatchedStepList() {
		return this.matchedStepList;
	}

	// Get exact step
	public static String getStepName(String myStep) {
		myStep = myStep.replaceAll(START_OR_END, "");
		return myStep;
	}

	// Get Source File
	public static String getSourceName(String mySource) {
		mySource = mySource.substring(mySource.lastIndexOf("/")).replace("/","");
		return mySource;
	}

	// Remove junk words from keyword list
	public void removeJunkWords(List<String> keywords) {
		List<String> junks = Arrays.asList(junkWords);
		keywords.removeAll(junks);
	}

	// Get all junkList
	public List<String> getJunkList() {
		List<String> junkList = Arrays.asList(junkWords);
		return junkList;
	}

	// Get Last word of a String by replacing KEYWORD
	public String lastPrefix(String string) {
		String lastStep = string.replaceFirst(KEYWORD_SPACE_REGEX, "");
		if(lastStep.contains(","))
			lastStep = lastStep.substring(lastStep.lastIndexOf(",")+1).trim();
		return lastStep;
	}

	
	// To get user-friendly step
	public String getUserStep(String stepText) {
		stepText = stepText.replace(INTEGER, "{integer-number}");
		stepText = stepText.replace(REAL_NUMBER, "{real-number}");
		stepText = stepText.replace(TEXT, "{text}");
		stepText = stepText.replace(ANY_TEXT, "{any-text}");
		stepText = stepText.replaceAll(START_OR_END, "");
		return stepText;
	}

	
	// Get Last word of a String
	/*
	 * public String lastPrefix(String string) { String lastWord = null; if(
	 * string.contains(" ")| string.endsWith(" ") ) { lastWord =
	 * string.substring(string.lastIndexOf(' '), string.length()); lastWord =
	 * lastWord.matches("(^\\s)[\\w]+")?lastWord=lastWord.trim():lastWord;
	 * return lastWord; } else return string; }
	 */

	/*
	 * //Get First word of a String public String firstWord(String string) {
	 * String fistString = null; if (string.contains(" ")) { int index =
	 * string.indexOf(" "); fistString = string.substring(0, index); return
	 * fistString; } else return string; }
	 */
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
