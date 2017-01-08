package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gherkin.formatter.model.Background;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.DescribedStatement;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public class PositionedElement {
	private BasicStatement statement;
	private int endOffset = -1;
	private IDocument document;
	private List<PositionedElement> children = new ArrayList<PositionedElement>();

	PositionedElement(IDocument doc, BasicStatement stmt) {
		this.statement = stmt;
		this.document = doc;
	}

	void addChild(PositionedElement child) {
		children.add(child);
	}
	
	private static int getDocumentLine(int line) {
		// numbering in document is 0-based;
		return line - 1;
	}

	void setEndLine(int lineNo) {
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
	
	public List<PositionedElement> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public boolean isBackground() {
		return statement instanceof Background;
	}
	
	public boolean isExamples() {
		return statement instanceof Examples;
	}

	public boolean isFeature() {
		return statement instanceof Feature;
	}

	public boolean isScenarioOutline() {
		return statement instanceof ScenarioOutline;
	}

	public boolean isScenario() {
		return statement instanceof Scenario;
	}

	public boolean isStep() {
		return statement instanceof Step;
	}
	
	public boolean isStepContainer() {
		return isBackground() || isScenarioOutline() || isScenario();
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
	
	@Override
	public String toString() {
		String result;
		
		if (statement instanceof DescribedStatement) {
			result = ((DescribedStatement) statement).getName();
			if ("".equals(result)) {
				result = statement.getKeyword();
			}
		}
		else if (statement instanceof Step) {
			result = ((Step) statement).getKeyword() + ((Step) statement).getName();
		}
		else {
			result = statement.toString();
		}
		
		return result;
	}
}