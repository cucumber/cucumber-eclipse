package cucumber.eclipse.editor.editors;

import static cucumber.eclipse.editor.editors.DocumentUtil.getDocumentLanguage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.markers.IMarkerManager;
import cucumber.eclipse.editor.markers.MarkerIds;
import cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants;
import cucumber.eclipse.editor.steps.IStepProvider;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

/**
 * @author andreas
 *
 */
public class GherkinErrorMarker implements Formatter {
	private final IMarkerManager markerManager;
	private final IFile file;
	private final IDocument document;
   
	private Set<cucumber.eclipse.steps.integration.Step> foundSteps = null;

	private boolean inScenarioOutline = false;

	private List<gherkin.formatter.model.Step> scenarioOutlineSteps;

	public GherkinErrorMarker(IStepProvider stepProvider, IMarkerManager markerManager, IFile inputfile, IDocument doc) 
	{
		this.markerManager = markerManager;
		this.file = inputfile;
		this.document = doc;
        this.foundSteps = stepProvider.getStepsInEncompassingProject();
	}

	public void removeExistingMarkers() {
		markerManager.removeAll(MarkerIds.LEXING_ERROR, file);
		markerManager.removeAll(MarkerIds.SYNTAX_ERROR, file);
		markerManager.removeAll(MarkerIds.UNMATCHED_STEP, file);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gherkin.formatter.Formatter#background(gherkin.formatter.model.Background
	 * )
	 */
	@Override
	public void background(Background arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#close()
	 */
	@Override
	public void close() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#done()
	 */
	@Override
	public void done() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#eof()
	 */
	@Override
	public void eof() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gherkin.formatter.Formatter#examples(gherkin.formatter.model.Examples)
	 */
	@Override
	public void examples(Examples examples) {
		ExamplesTableRow examplesHeader = examples.getRows().get(0);
		for (int i = 1; i < examples.getRows().size(); i++) {
			ExamplesTableRow currentExample = examples.getRows().get(i);
			matchScenarioOutlineExample(examplesHeader, currentExample);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#feature(gherkin.formatter.model.Feature)
	 */
	@Override
	public void feature(Feature arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gherkin.formatter.Formatter#scenario(gherkin.formatter.model.Scenario)
	 */
	@Override
	public void scenario(Scenario arg0) {
		inScenarioOutline = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#scenarioOutline(gherkin.formatter.model.
	 * ScenarioOutline)
	 */
	@Override
	public void scenarioOutline(ScenarioOutline arg0) {
		inScenarioOutline = true;
		
		scenarioOutlineSteps = new ArrayList<gherkin.formatter.model.Step>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#step(gherkin.formatter.model.Step)
	 */
	@Override
	public void step(Step stepLine) {
		if (!inScenarioOutline) {
			validateStep(stepLine);
		} else {
			scenarioOutlineSteps.add(stepLine);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#syntaxError(java.lang.String,
	 * java.lang.String, java.util.List, java.lang.String, java.lang.Integer)
	 */
	@Override
	public void syntaxError(String state, String event,
			List<String> legalEvents, String uri, Integer line) {

		StringBuffer buf = new StringBuffer("Syntax Error: Expected one of ");
		for (String ev : legalEvents) {
			buf.append(ev);
			buf.append(", ");
		}
		buf.replace(buf.length() - 3, buf.length(), " but got ");
		buf.append(event);
		
		try {
			markerManager.add(MarkerIds.SYNTAX_ERROR,
					file,
					IMarker.SEVERITY_ERROR,
					buf.toString(),
					line,
					document.getLineOffset(line - 1),
					document.getLineOffset(line - 1) + document.getLineLength(line - 1));
		} catch (BadLocationException e) {
			// Ignore for now.
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#uri(java.lang.String)
	 */
	@Override
	public void uri(String arg0) {
	}

	private Map<String, String> getExampleVariablesMap(ExamplesTableRow header, ExamplesTableRow values) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (int i = 0; i < header.getCells().size(); i++) {
			result.put(header.getCells().get(i), values.getCells().get(i));
		}
		return result;
	}

	private void matchScenarioOutlineExample(ExamplesTableRow header, ExamplesTableRow example) {
		Map<String, String> exampleVariablesMap = getExampleVariablesMap(header, example);
		for (gherkin.formatter.model.Step scenarioOutlineStepLine : scenarioOutlineSteps) {
			validateStep(scenarioOutlineStepLine, exampleVariablesMap, example.getLine());
		}
	}

	private String getResolvedStepStringForExample(Step stepLine, Map<String, String> examplesLineMap) {
		String stepString = stepLine.getKeyword() + stepLine.getName();
		if (examplesLineMap != null) {
			for (Map.Entry<String, String> examplesLineEntry : examplesLineMap.entrySet()) {
				stepString = stepString.replace("<" + examplesLineEntry.getKey() + ">", examplesLineEntry.getValue());
			}
		}
		return stepString;
	}
	
	public void validateStep(Step stepLine) {
		validateStep(stepLine, null, -1);
	}
	
	
	
	public void validateStep(Step stepLine, Map<String, String> examplesLineMap, int currentLine) {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if (store.getBoolean(ICucumberPreferenceConstants.PREF_CHECK_STEP_DEFINITIONS)) {
			
			if ("".equals(stepLine.getName())) {
				try {
					markMissingStepName(file, document, stepLine);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
				return;
			}

			String stepString = getResolvedStepStringForExample(stepLine, examplesLineMap);
			cucumber.eclipse.steps.integration.Step step = new StepMatcher().matchSteps(getDocumentLanguage(document), foundSteps, stepString);
			step = new StepMatcher().matchSteps(getDocumentLanguage(document), foundSteps, stepString);
			
			if (step == null) {
				try {
					markUnmatchedStep(file, document, stepLine, inScenarioOutline ? currentLine : -1);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	private void markMissingStepName(IFile featureFile, IDocument doc, gherkin.formatter.model.Step stepLine)
			throws BadLocationException {
		
		int line = stepLine.getLine() - 1;
		IRegion lineRegion = doc.getLineInformation(line);
		
		markerManager.add(MarkerIds.SYNTAX_ERROR,
				featureFile,
				IMarker.SEVERITY_WARNING,
				"No step name.",
				line,
				lineRegion.getOffset(),
				lineRegion.getOffset() + lineRegion.getLength());
	}

	private void markUnmatchedStep(IFile featureFile, IDocument doc, gherkin.formatter.model.Step stepLine,
			int exampleLine) throws BadLocationException {
		
		FindReplaceDocumentAdapter find = new FindReplaceDocumentAdapter(doc);
		IRegion region = find.find(doc.getLineOffset(stepLine.getLine() - 1),
				stepLine.getName(), true, true, false, false);

		String warningMessage = String.format(
				"Step '%s' does not have a matching glue code%s",
				stepLine.getName(),
				(inScenarioOutline ? " for example on line " + exampleLine : ""));

		markerManager.add(MarkerIds.UNMATCHED_STEP,
				featureFile,
				IMarker.SEVERITY_WARNING,
				warningMessage,
				stepLine.getLine(),
				region.getOffset(),
				region.getOffset() + region.getLength());
	}

	@Override
	public void endOfScenarioLifeCycle(Scenario arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startOfScenarioLifeCycle(Scenario arg0) {
		// TODO Auto-generated method stub
		
	}
}
