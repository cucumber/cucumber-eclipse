package cucumber.eclipse.editor.editors;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.editor.markers.MarkerIds;
import cucumber.eclipse.editor.markers.MarkerManager;
import cucumber.eclipse.editor.steps.GlueRepository;
import cucumber.eclipse.editor.steps.GlueRepository.Glue;
import cucumber.eclipse.steps.integration.Activator;
import gherkin.lexer.LexingError;
import gherkin.parser.Parser;

public class StepHyperlinkDetector implements IHyperlinkDetector {

	public StepHyperlinkDetector() {
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();
		if (document == null) {
			return null;
		}

		int offset = region.getOffset();
		int lineStartOffset = 0;
		
		IRegion lineInfo = null;
		String currentLine = null;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			lineStartOffset = lineInfo.getOffset();
			currentLine = document.get(lineStartOffset, lineInfo.getLength());
		} catch (BadLocationException e) {
			return null;
		}

		
		// find the gherkin step
		// get the related step definition
		// get the related step definitions file
		// open this last one
		
		Glue glue = GlueRepository.INSTANCE.findGlue(currentLine.trim());
		if(glue == null) {
			// no glue found
			return null;
		}
		
		// define the hyperlink region
		String textStatement = glue.getGherkinStepWrapper().getStep().getName();
		int statementStartOffset = lineStartOffset + currentLine.indexOf(textStatement);

		IRegion stepRegion = new Region(statementStartOffset, textStatement.length());

		return new IHyperlink[] { new StepHyperlink(stepRegion, glue.getStepDefinition()) };
	}
	
	// go through all examples of current scenario outline and generate step strings with replaced variables values
	protected static List<String> resolveLineStep(IEditorPart editorPart, int currentLineNumber) {
		ITextEditor editor = (ITextEditor) editorPart;

		IDocument document = editor.getDocumentProvider().getDocument(editorPart.getEditorInput());
		
		IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
		IFile featureFile = fileEditorInput.getFile();
		MarkerManager markerManager = new MarkerManager();
		PopupMenuFindStepFormatter findStepFormatter = new PopupMenuFindStepFormatter(currentLineNumber);
		Parser p = new Parser(findStepFormatter, false);
		try {
			p.parse(document.get(), "", 0);
		} catch (LexingError l) {
			markerManager.add(MarkerIds.LEXING_ERROR, featureFile, IMarker.SEVERITY_ERROR, l.getLocalizedMessage(), 1, 0, 0);
		}
		return findStepFormatter.getResolvedStepNames();
	}
	
}
