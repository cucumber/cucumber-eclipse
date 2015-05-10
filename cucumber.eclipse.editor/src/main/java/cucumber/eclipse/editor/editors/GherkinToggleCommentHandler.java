package cucumber.eclipse.editor.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class GherkinToggleCommentHandler extends AbstractHandler {
	private static final char EMPTY_CHAR = ' ';
	private static final char COMMENT_CHAR = '#';

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ITextEditor editor = (ITextEditor) HandlerUtil
				.getActiveEditorChecked(event);

		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		TextSelection selection = (TextSelection) selectionProvider
				.getSelection();
		IDocument doc = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());

		toggleComments(doc, selection);
		selectionProvider.setSelection(selection);

		return null;
	}

	private void toggleComments(IDocument doc, TextSelection selection) {
		try {
			boolean shouldComment = shouldComment(doc, selection);
			if (shouldComment) {
				commentSelectedLines(doc, selection);
			} else {
				uncommentSelectedLines(doc, selection);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private boolean shouldComment(IDocument doc, TextSelection selection)
			throws BadLocationException {
		boolean shouldComment = false;
		for (int i = selection.getStartLine(); i < selection.getEndLine() + 1; i++) {
			int offset = doc.getLineOffset(i);
			while (offset < doc.getLineOffset(i) + doc.getLineLength(i)) {
				if (doc.getChar(offset) != EMPTY_CHAR) {
					if (doc.getChar(offset) != COMMENT_CHAR) {
						shouldComment = true;
					}
					break;
				}
				offset++;
			}
		}
		return shouldComment;
	}

	private void commentSelectedLines(IDocument doc, TextSelection selection)
			throws BadLocationException {
		StringBuilder content = new StringBuilder(doc.get());
		int addedChars = 0;
		for (int i = selection.getStartLine(); i < selection.getEndLine() + 1; i++) {
			int offset = doc.getLineOffset(i);
			while (offset < doc.getLineOffset(i) + doc.getLineLength(i)) {
				if (doc.getChar(offset) != ' ') {
					if (doc.getChar(offset) != COMMENT_CHAR) {
						content.insert(offset + addedChars, COMMENT_CHAR);
						addedChars++;
					}
					break;
				}
				offset++;
			}
		}
		doc.set(content.toString());
	}

	private void uncommentSelectedLines(IDocument doc, TextSelection selection)
			throws BadLocationException {
		StringBuilder content = new StringBuilder(doc.get());
		int removedChars = 0;
		for (int i = selection.getStartLine(); i < selection.getEndLine() + 1; i++) {
			int offset = doc.getLineOffset(i);
			while (offset < doc.getLineOffset(i) + doc.getLineLength(i)) {
				if (doc.getChar(offset) != EMPTY_CHAR) {
					if (doc.getChar(offset) == COMMENT_CHAR) {
						content.deleteCharAt(offset - removedChars);
						removedChars++;
					}
					break;
				}
				offset++;
			}
		}
		doc.set(content.toString());
	}
}
