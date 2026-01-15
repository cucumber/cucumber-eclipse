package io.cucumber.eclipse.java.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.search.ui.text.Match;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.java.plugins.MatchedStep;

/**
 * Represents a match of a Cucumber step definition in a feature file.
 * <p>
 * This match class provides the location information needed to display
 * the reference in Eclipse's search results view.
 * </p>
 * 
 * @author christoph
 */
public class CucumberStepMatch extends Match {
	
	private final int lineNumber;
	private final MatchedStep<?> matchedStep;
	
	/**
	 * Creates a new match for a Cucumber step in a feature file.
	 * 
	 * @param featureFile the feature file containing the step
	 * @param lineNumber the line number of the step (1-based)
	 * @param matchedStep the matched step information
	 */
	public CucumberStepMatch(IResource featureFile, int lineNumber, MatchedStep<?> matchedStep) {
		super(featureFile, calculateOffset(featureFile, lineNumber), calculateLength(featureFile, lineNumber));
		this.lineNumber = lineNumber;
		this.matchedStep = matchedStep;
	}
	
	/**
	 * Calculates the offset in the document for the given line number.
	 */
	private static int calculateOffset(IResource resource, int lineNumber) {
		if (!(resource instanceof IFile)) {
			return 0;
		}
		
		try {
			GherkinEditorDocument editorDoc = GherkinEditorDocumentManager.get(resource, false);
			if (editorDoc != null) {
				IDocument document = editorDoc.getDocument();
				// Line numbers from Cucumber are 1-based, Eclipse is 0-based
				IRegion lineInfo = document.getLineInformation(lineNumber - 1);
				return lineInfo.getOffset();
			}
		} catch (BadLocationException e) {
			// Ignore - fall back to 0
		}
		
		return 0;
	}
	
	/**
	 * Calculates the length of text to highlight for the match.
	 */
	private static int calculateLength(IResource resource, int lineNumber) {
		if (!(resource instanceof IFile)) {
			return 0;
		}
		
		try {
			GherkinEditorDocument editorDoc = GherkinEditorDocumentManager.get(resource, false);
			if (editorDoc != null) {
				IDocument document = editorDoc.getDocument();
				// Line numbers from Cucumber are 1-based, Eclipse is 0-based
				IRegion lineInfo = document.getLineInformation(lineNumber - 1);
				return lineInfo.getLength();
			}
		} catch (BadLocationException e) {
			// Ignore - fall back to 0
		}
		
		return 0;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public MatchedStep<?> getMatchedStep() {
		return matchedStep;
	}
}
