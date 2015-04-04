package cucumber.eclipse.editor.editors;

import gherkin.formatter.model.BasicStatement;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

class PositionedElement {
	private BasicStatement statement;
	private int endOffset = -1;
	private IDocument document;

	public PositionedElement(IDocument doc, BasicStatement stmt) {
		this.statement = stmt;
		this.document = doc;
	}

	private static int getDocumentLine(int line) {
		// numbering in document is 0-based;
		return line - 1;
	}

	public void setEndLine(int lineNo) {
		try {
			endOffset = document.getLineOffset(getDocumentLine(lineNo))
					+ document.getLineLength(getDocumentLine(lineNo));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BasicStatement getStatement() {
		return statement;
	}

	public Position toPosition() throws BadLocationException {
		int offset = document.getLineOffset(getDocumentLine(statement
				.getLine()));
		if (endOffset == -1) {
			endOffset = offset
					+ document.getLineLength(getDocumentLine(statement
							.getLine()));
		}

		return new Position(offset, endOffset - offset);
	}
}