/**
 * 
 */
package cucumber.eclipse.editor.contentassist;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

import cucumber.eclipse.editor.steps.UniversalStepDefinitionsProvider;
import cucumber.eclipse.steps.integration.StepDefinition;
import gherkin.I18n;

/**
 * @author girija.panda@nokia.com
 * 
 *         Purpose: Supports Content-Assistance of any Feature File for CTRL+
 *         SPACE key Content-Assistance : Keyword-Assistance and
 *         Step-Assistance based on user input 1. Collect all steps from
 *         step-definition-java file 2. Remove junk-words from Keyword List 3.
 *         Populating of Keywords/Steps based on various RegEx pattern
 * 
 *         Content-Assistance Supports : To Populate Step Proposals From Both
 *         Current-Project and External-Dependency(JAR/POM)
 */
public class CucumberContentAssist {

	private final String NOSTEPS = "No Step Definition Found";

	// Junk words needs to be filter form keyword list
	private String[] junkWords = { "* ", "Business Need", "Ability", "Scenario Template", "Scenarios" };

	// RegEx1 : (Line starts with only a <Step-keyword>[without <space> or
	// <word>]) OR
	// (Line doesn't starts with a <Step-keyword>)
//	public final String KEYWORD_REGEX = "(^Given|When|Then|And|But|$[\\S\\W\\D])|(^(?!Given|When|Then|And|But).+)";

	// RegEx2 : Line starts with <Step-Keyword>+<space>+<any-word>
//	public final String KEYWORD_SPACE_WORD_REGEX = "^(Given|When|Then|And|But)[\\s][\\w\\d\\s\\S]*";

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
//	public final String KEYWORD_SPACE_REGEX = "^(Given|When|Then|And|But)\\s+";

	// Regex-9 : Any Word/Digit/NonWord/Space
	public final String ANY = "[\\w\\d\\S][\\,]*";

	public final String COMMA_SPACE_REGEX = "(\\,|\\s$)+";

	// Integer RegEx: (\d+)OR(\\d+)
	//public final static String INTEGER_REGEX = "\\(\\d\\+\\)|\\(\\\\d\\+\\)";
	public final static String INTEGER = "(\\d+)";
	// Real Number
	public final static String REAL_NUMBER = "(\\d+\\.\\d+)";
	// Text
	public final static String TEXT = "([\"]*)";
	// Any Text
	public final static String ANY_TEXT = "(.*?)";
	// Start Or End
	public final static String START_OR_END = "^\\^|\\$$";

	// Newly Added to replace '<p>|' from step
	public final static String P_CHAR = "<p>|";

	// To collect Steps
	private List<String> stepDetailList = null;
	private List<String> matchedStepList = null;
	private CompletionProposal allStepsProposal = null;
	private CompletionProposal noProposal = null;
	CompletionProposal matchedStepsProposal = null;
	private Set<StepDefinition> importedSteps = null;

	// Initialize
	public CucumberContentAssist(String lang, IProject project) throws CoreException {
		this.importedSteps = UniversalStepDefinitionsProvider.INSTANCE.getStepDefinitions(project);

		// System.out.println("CucumberContentAssist:importedSteps:"
		// +importedSteps);

		// Step lists
		this.stepDetailList = new ArrayList<String>();
		this.matchedStepList = new ArrayList<String>();
	}

	public List<String> getStepKeyWords(I18n i18n) {
		List<String> stepKeywords = i18n.getStepKeywords();
		stepKeywords.removeAll(asList(junkWords));
		return stepKeywords;
	}

	// Iterate and collect all step proposals from Both Source and JAR
	public void collectAllSteps(String matchPrefix) {

		/*
		 * Commented for Performance StepDetailList = new ArrayList<String>();
		 * matchedStepList = new ArrayList<String>(); //Collection of all steps
		 * importedSteps = StepDefinitions.steps;
		 */

		String lineNumber = "";
		String stepDetailInfo = null;
		String stepSource = null;
		String packageName = null;

		if (!importedSteps.isEmpty()) {

			// To Collect All Steps
			for (StepDefinition step : importedSteps) {

				// Get Step-Text
				String stepText = getStepName(step.getText());

				// Collect Steps from Source(.java) file Based on LineNumber
				if (step.getLineNumber() != 0) {
					if (step.getSource() != null)
						stepSource = getSourceName(step.getSource().toString()); // Source
																					// of
																					// Cuke-Step-Definition
																					// file
					/*
					 * else stepSource =
					 * getSourceName(step.getJava8CukeSource()); // Source of
					 * Java8-Cuke-Step-Definition file
					 */

					lineNumber = step.getLineNumber() + ""; // Line-Number
					stepDetailInfo = stepText + " : " + stepSource + ":" + lineNumber;

					// Don't Delete : Used For Debug
					// System.out.println("CucumberContentAssist:STEP-TEXT:"+stepText);
				}

				// Collect Steps from external JAR(.class) file
				else {
					stepSource = getSourceName(step.getSourceName());
					packageName = step.getPackageName();
					stepDetailInfo = stepText + " : " + stepSource + ":" + packageName;

					// Don't Delete : Used For Debug
					// System.out.println("collectAllSteps:ELSE:stepDetailInfo:"
					// +stepDetailInfo);
				}

				// Don't Delete : Used For Debug
				// System.out.println("StepDetailInfo :" +stepDetailInfo);
				// System.out.println("*******************************************************************");

				// Collect only matched steps
				/*
				 * if(stepText.startsWith(matchPrefix) &&
				 * (!matchPrefix.matches(ENDSWITH_ANYSPACE))) {
				 * matchedStepList.add(stepDetailInfo); }
				 */

				// For starts-with prefix
				if (stepText.startsWith(matchPrefix) && matchPrefix.matches(STARTS_ANY)) {
					this.matchedStepList.add(stepDetailInfo);
				}

				// For contains prefix
				else if (stepText.contains(matchPrefix)) {
					this.matchedStepList.add(stepDetailInfo);
				}

				// Collect all steps
				this.stepDetailList.add(stepDetailInfo);
			}
		}

	}

	// Populate all step proposals from both Source and External-JAR
	public void importStepProposals(String prefix, String preStep, int offset, Image ICON, String stepDetail,
			ArrayList<ICompletionProposal> result) {

		String stepWithSource = null;
		String stepText = null;
		String stepSource = null;
		String lineNumber = null;
		String stepDetails = null;
		String packageName = null;

		// Split the DetailStep based on char(:)
		stepWithSource = stepDetail.substring(0, stepDetail.lastIndexOf(":")).trim();
		stepText = stepWithSource.substring(0, stepWithSource.lastIndexOf(":")).trim();
		stepSource = stepWithSource.substring(stepWithSource.lastIndexOf(":") + 1).trim();

		// Steps from Source(.java) file
		if (stepSource.endsWith(".java")) {
			lineNumber = stepDetail.substring(stepDetail.lastIndexOf(":") + 1).trim();
			stepDetails = "Step : " + stepText + "\nSource : " + stepSource + "\nLine Number : " + lineNumber;
			// System.out.println("importStepProposals:IF:stepDetails:"
			// +stepDetails);
		}

		// Steps from External-JAR(.class) file
		else if (stepSource.endsWith(".class")) {

			packageName = stepDetail.substring(stepDetail.lastIndexOf(":") + 1).trim();
			stepDetails = "Step : " + stepText + "\nSource : " + stepSource + "\nPackage : " + packageName;
		}

		// Don't Delete : Used For Debug
		// System.out.println("ALL_STEP_DETAILS ="+stepDetails);
		// System.out.println("PREFIX ="+prefix);
		// if(!prefix.startsWith(" ")) {

		// For replace entire prefix
		if (preStep.length() > prefix.length()) {
			// Don't Delete : Used For Debug
			// System.out.println("IF : PREFIX =" + prefix);
			// System.out.println("IF : PRESTEP =" + preStep);
			// System.out.println("IF : OFFSET =" + offset);
			allStepsProposal = new CompletionProposal(getUserStep(stepText), offset - prefix.length(), preStep.length(),
					stepText.length(), ICON, stepWithSource, null, stepDetails);

		}

		// For starts-with and contains prefix based on offset
		else {
			// Don't Delete : Used For Debug
			// System.out.println("ELSE : PREFIX =" + prefix);
			// System.out.println("ELSE : PRESTEP =" + preStep);
			// System.out.println("ELSE : OFFSET =" + offset);
			if (stepText.startsWith(prefix)) {
				// Don't Delete : Used For Debug
				// System.out.println("ELSE IF startsWith : PREFIX =" + prefix);
				allStepsProposal = new CompletionProposal(getUserStep(stepText).replace(prefix, ""), offset, 0,
						stepText.length(), ICON, stepWithSource, null, stepDetails);
			} else if (stepText.contains(prefix)) {
				// Don't Delete : Used For Debug
				// System.out.println("ELSE IF ELSE IF contains : PREFIX =" +
				// prefix);
				allStepsProposal = new CompletionProposal(getUserStep(stepText), offset - prefix.length(),
						preStep.length(), stepText.length(), ICON, stepWithSource, null, stepDetails);
			}
		}

		result.add(allStepsProposal);
	}

	// No Proposals
	public void displayNoProposal(int offset, Image ICON, ArrayList<ICompletionProposal> result) {
		this.noProposal = new CompletionProposal("", offset, 0, 0, ICON, NOSTEPS, null, null);
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
		if (mySource.contains("/"))
			mySource = mySource.substring(mySource.lastIndexOf("/")).replace("/", "");
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
	public String lastPrefix(String string, List<String> keywords) {
		String longestMatch = null;
		for (String keyword : keywords) {
			if (string.startsWith(keyword)) {
				if (longestMatch == null || keyword.length()>longestMatch.length()) {
					longestMatch = keyword;
				}
			}
		}
		//replace the keyword
		String lastStep = longestMatch!=null?string.substring(longestMatch.length()):string;
		if (lastStep.contains(","))
			lastStep = lastStep.substring(lastStep.lastIndexOf(",") + 1).trim();
		return lastStep;
	}

	// To get user-friendly step
	public String getUserStep(String stepText) {
		// Newly Added To Remove '<p>|' From Step
		stepText = stepText.replace(P_CHAR, "");
		stepText = stepText.replace(INTEGER, "{integer-number}");
		stepText = stepText.replace(REAL_NUMBER, "{real-number}");
		stepText = stepText.replace(TEXT, "{text}");
		stepText = stepText.replace(ANY_TEXT, "{any-text}");
		stepText = stepText.replaceAll(START_OR_END, "");
		//System.out.println("stepText = " +stepText);
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
