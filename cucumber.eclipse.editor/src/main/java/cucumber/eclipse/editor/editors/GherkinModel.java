package cucumber.eclipse.editor.editors;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.DescribedStatement;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.lexer.LexingError;
import gherkin.parser.ParseError;
import gherkin.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public class GherkinModel {

	private List<PositionedElement> elements = new ArrayList<PositionedElement>();
	
	public PositionedElement getFeatureElement() {
		return elements.isEmpty() ? null : elements.get(0);
	}
	
	public List<Position> getFoldRanges() {
		List<Position> foldRanges = new ArrayList<Position>();
		for (PositionedElement element : elements) {
			if (element.isFeature() || element.isStepContainer() || element.isExamples()) {
				try {
					foldRanges.add(element.toPosition());
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return foldRanges;
	}
	
	public PositionedElement getStepElement(int offset) throws BadLocationException {
		for (PositionedElement element : elements) {
			if (element.isStep() && element.toPosition().includes(offset)) {
				return element;
			}
		}
		
		return null;
	}

	public void updateFromDocument(final IDocument document) {
		elements.clear();
		
		Parser p = new Parser(new Formatter() {

			private Stack<PositionedElement> stack = new Stack<PositionedElement>();

			@Override
			public void uri(String arg0) {
			}

			@Override
			public void syntaxError(String arg0, String arg1,
					List<String> arg2, String arg3, Integer arg4) {
			}

			@Override
			public void step(Step arg0) {
				PositionedElement element = newPositionedElement(arg0);
				stack.peek().addChild(element);
				stack.peek().setEndLine(arg0.getLineRange().getLast());
			}

			@Override
			public void scenarioOutline(ScenarioOutline arg0) {
				handleStepContainer(arg0);
			}

			private void handleStepContainer(DescribedStatement stmt) {
				if (stack.peek().isStepContainer()) {
					stack.pop();
				}
				PositionedElement element = newPositionedElement(stmt);
				stack.peek().addChild(element);
				stack.push(element);
			}

			@Override
			public void scenario(Scenario arg0) {
				handleStepContainer(arg0);
			}

			@Override
			public void feature(Feature arg0) {
				stack.push(newPositionedElement(arg0));
			}

			@Override
			public void examples(Examples arg0) {
				int lastLine = getLastExamplesLine(arg0);
				newPositionedElement(arg0).setEndLine(lastLine);
				stack.peek().setEndLine(lastLine);
			}

			@Override
			public void eof() {
				while (!stack.isEmpty()) {
					stack.pop().setEndLine(document.getNumberOfLines());
				}
			}

			@Override
			public void done() {
			}

			@Override
			public void close() {
			}

			@Override
			public void background(Background arg0) {
				handleStepContainer(arg0);
			}
			
			private PositionedElement newPositionedElement(BasicStatement stmt) {
				PositionedElement element = new PositionedElement(document, stmt);
				elements.add(element);
				return element;
			}

			private int getLastExamplesLine(Examples examples) {
				int lastline = examples.getLineRange().getLast();
				if (!examples.getRows().isEmpty()) {
					lastline = examples.getRows().get(examples.getRows().size() - 1).getLine(); 
				}
				return lastline;
			}

			@Override
			public void endOfScenarioLifeCycle(Scenario arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void startOfScenarioLifeCycle(Scenario arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		try {
			p.parse(document.get(), "", 0);
		} catch (LexingError le) {
			// TODO: log
		} catch (ParseError pe) {
			// TODO: log
		}
	}
}
