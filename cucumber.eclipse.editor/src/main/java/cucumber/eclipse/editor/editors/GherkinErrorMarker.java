package cucumber.eclipse.editor.editors;

import static cucumber.eclipse.editor.editors.DocumentUtil.getDocumentLanguage;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import cucumber.eclipse.editor.markers.IMarkerManager;
import cucumber.eclipse.editor.steps.IStepProvider;

/**
 * @author andreas
 *
 */
public class GherkinErrorMarker implements Formatter {

	private static final String ERROR_ID = "cucumber.eclipse.editor.editors.Editor.syntaxerror";

	private static final String UNMATCHED_STEP_ERROR_ID = "cucumber.eclipse.editor.editors.Editor.unmatchedsteperror";

	private final IStepProvider stepProvider;
	private final IMarkerManager markerManager;
	private final IFile file;
	private final IDocument document;

	public GherkinErrorMarker(IStepProvider stepProvider, IMarkerManager markerManager, IFile inputfile,
			IDocument doc) {
		this.stepProvider = stepProvider;
		this.markerManager = markerManager;
		this.file = inputfile;
		this.document = doc;
	}

	public void removeExistingMarkers() {
		markerManager.removeAll(ERROR_ID, file);
		markerManager.removeAll(UNMATCHED_STEP_ERROR_ID, file);
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
	public void examples(Examples arg0) {
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
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#scenarioOutline(gherkin.formatter.model.
	 * ScenarioOutline)
	 */
	@Override
	public void scenarioOutline(ScenarioOutline arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gherkin.formatter.Formatter#step(gherkin.formatter.model.Step)
	 */
	@Override
	public void step(Step stepLine) {
		String stepString = stepLine.getKeyword() + stepLine.getName();
		cucumber.eclipse.steps.integration.Step step = new StepMatcher().matchSteps(
				getDocumentLanguage(document), stepProvider.getStepsInEncompassingProject(file),
				stepString);
		if (step == null) {
			try {
				markUnmatchedStep(file, document, stepLine);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
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
			markerManager.add(ERROR_ID,
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

	private void markUnmatchedStep(IFile featureFile, IDocument doc,
			gherkin.formatter.model.Step stepLine) throws BadLocationException,
			CoreException {
		
		FindReplaceDocumentAdapter find = new FindReplaceDocumentAdapter(doc);
		IRegion region = find.find(doc.getLineOffset(stepLine.getLine() - 1),
				stepLine.getName(), true, true, false, false);

		markerManager.add(UNMATCHED_STEP_ERROR_ID,
				featureFile,
				IMarker.SEVERITY_WARNING,
				"Step does not have a matching glue code.",
				stepLine.getLine(),
				region.getOffset(),
				region.getOffset() + region.getLength());
	}
}
