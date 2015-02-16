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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

/**
 * @author andreas
 *
 */
public class GherkinReconcilingStrategy implements IReconcilingStrategy,
		IReconcilingStrategyExtension {
	private IDocument document;
	private Editor editor;

	public GherkinReconcilingStrategy(Editor editor) {
		this.editor = editor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#
	 * setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	protected static class PositionedElement {
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

		public void setEndLine(int lineNo) throws BadLocationException {
			endOffset = document.getLineOffset(getDocumentLine(lineNo))
					+ document.getLineLength(getDocumentLine(lineNo));
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#
	 * initialReconcile()
	 */
	@Override
	public void initialReconcile() {
		final List<Position> ranges = new ArrayList<Position>();
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
			}

			private boolean isStepContainer(BasicStatement stmt) {
				return stmt instanceof Scenario
						|| stmt instanceof ScenarioOutline
						|| stmt instanceof Background;
			}

			private boolean isExamples(BasicStatement stmt) {
				return stmt instanceof Examples;
			}

			@Override
			public void scenarioOutline(ScenarioOutline arg0) {
				try {
					handleStepContainer(arg0);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void handleStepContainer(DescribedStatement stmt)
					throws BadLocationException {
				if (isExamples(stack.peek().getStatement())) {
					PositionedElement pos = stack.pop();
					pos.setEndLine(stmt.getLine() - 1);
					ranges.add(pos.toPosition());
				}
				if (isStepContainer(stack.peek().getStatement())) {
					PositionedElement pos = stack.pop();
					pos.setEndLine(stmt.getLine() - 1);
					ranges.add(pos.toPosition());
				}
				stack.push(new PositionedElement(document, stmt));
			}

			@Override
			public void scenario(Scenario arg0) {
				try {
					handleStepContainer(arg0);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void feature(Feature arg0) {
				stack.push(new PositionedElement(document, arg0));
			}

			@Override
			public void examples(Examples arg0) {
				stack.push(new PositionedElement(document, arg0));
			}

			@Override
			public void eof() {
				int lastline = document.getNumberOfLines();
				while (!stack.isEmpty()) {
					PositionedElement pos = stack.pop();
					try {
						pos.setEndLine(lastline);
						ranges.add(pos.toPosition());
					} catch (BadLocationException e) {
					}
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
				try {
					handleStepContainer(arg0);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		try {
			p.parse(document.get(), "", 0);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					editor.updateFoldingStructure(ranges);
				}

			});

		} catch (LexingError le) {
			// TODO: log
		} catch (ParseError pe) {
			// TODO: log
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org
	 * .eclipse.jface.text.IDocument)
	 */
	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.
	 * eclipse.jface.text.reconciler.DirtyRegion,
	 * org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.
	 * eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(IRegion partition) {
		initialReconcile();
	}

}
