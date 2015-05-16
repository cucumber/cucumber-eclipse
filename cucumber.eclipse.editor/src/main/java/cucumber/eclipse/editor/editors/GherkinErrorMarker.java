package cucumber.eclipse.editor.editors;

import static cucumber.eclipse.editor.editors.FeatureFileUtil.getDocumentLanguage;
import static cucumber.eclipse.editor.editors.FeatureFileUtil.getStepsInEncompassingProject;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.MarkerUtilities;

import cucumber.eclipse.editor.Activator;

/**
 * @author andreas
 *
 */
public class GherkinErrorMarker implements Formatter {

	private static final String ERROR_ID = "cucumber.eclipse.editor.editors.Editor.syntaxerror";

	private static final String UNMATCHED_STEP_ERROR_ID = "cucumber.eclipse.editor.editors.Editor.unmatchedsteperror";

	IEditorPart editor;
	IFile file;
	IDocument document;

	public GherkinErrorMarker(IEditorPart editor, IFile inputfile, IDocument doc) {
		this.editor = editor;
		this.file = inputfile;
		this.document = doc;
	}

	public void removeExistingMarkers() {
		try {
			file.deleteMarkers(ERROR_ID, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
		deleteUnmatchedStepsMarkers(file);
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
				getDocumentLanguage(editor), getStepsInEncompassingProject(file),
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
		int docline = line - 1;
		Map<String, Object> map = new HashMap<String, Object>();
		MarkerUtilities.setLineNumber(map, docline);
		StringBuffer buf = new StringBuffer();
		buf.append("Syntax Error: Expected one of ");
		for (String ev : legalEvents) {
			buf.append(ev);
			buf.append(", ");
		}
		buf.replace(buf.length() - 3, buf.length(), " but got ");
		buf.append(event);
		MarkerUtilities.setMessage(map, buf.toString());
		map.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		map.put(IMarker.LOCATION, file.getFullPath().toString());
		try {
			MarkerUtilities.setCharStart(map, document.getLineOffset(docline));
			MarkerUtilities.setCharEnd(map, document.getLineOffset(docline) + document.getLineLength(docline));
		}catch(BadLocationException e) {
			// Ignore for now.
		}
		try {
			MarkerUtilities.createMarker(file, map, ERROR_ID);
		} catch (CoreException ce) {
			Activator.getDefault().getLog().log(ce.getStatus());
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

	private void deleteUnmatchedStepsMarkers(IFile featureFile) {
		try {
			featureFile.deleteMarkers(UNMATCHED_STEP_ERROR_ID, true,
					IResource.DEPTH_ZERO);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	private void markUnmatchedStep(IFile featureFile, IDocument doc,
			gherkin.formatter.model.Step stepLine) throws BadLocationException,
			CoreException {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		attributes.put(IMarker.LINE_NUMBER, stepLine.getLine());
		attributes.put(IMarker.MESSAGE,
				"Step does not have a matching glue code.");

		FindReplaceDocumentAdapter find = new FindReplaceDocumentAdapter(doc);
		IRegion region = find.find(0, stepLine.getName(), true, true, false,
				false);

		attributes.put(IMarker.CHAR_START, region.getOffset());
		attributes.put(IMarker.CHAR_END,
				region.getOffset() + region.getLength());
		MarkerUtilities.createMarker(featureFile, attributes,
				UNMATCHED_STEP_ERROR_ID);
	}
}
