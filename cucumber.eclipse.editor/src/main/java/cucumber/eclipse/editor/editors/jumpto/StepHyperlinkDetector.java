package cucumber.eclipse.editor.editors.jumpto;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import cucumber.eclipse.editor.steps.GlueRepository;
import cucumber.eclipse.editor.steps.GlueRepository.Glue;

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
	
	
		
}
