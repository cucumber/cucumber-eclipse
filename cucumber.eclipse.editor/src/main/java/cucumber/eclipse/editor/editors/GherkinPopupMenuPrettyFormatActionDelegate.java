package cucumber.eclipse.editor.editors;

import gherkin.lexer.LexingError;
import gherkin.parser.ParseError;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import cucumber.eclipse.editor.Activator;

public class GherkinPopupMenuPrettyFormatActionDelegate implements
		IEditorActionDelegate {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void run(IAction action) {

		IEditorPart editorPart = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextEditor editor = (ITextEditor) editorPart;
		Shell shell = editorPart.getSite().getShell();
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		String contents = doc.get();
		try {
			String formatted = GherkinFormatterUtil.format(contents);
			doc.replace(0, doc.getLength(), formatted);
		} catch (ParseError e) {
			MessageDialog.openInformation(shell, "Unable to pretty format.",
					"One can only format a feature file that has no parse errors: \n"
							+ "The following parse error was encountered: ["
							+ e.getMessage() + "]");

		} catch (LexingError e) {
			MessageDialog.openInformation(shell, "Unable to pretty format.",
					"One can only format a feature file that has no lexing errors: \n"
							+ "The following lex error was encountered: ["
							+ e.getMessage() + "]");
		}
		
		catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart part) {

	}

}
