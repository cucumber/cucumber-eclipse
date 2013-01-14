package cucumber.eclipse.editor.editors;


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
		
		GherkinFormatterUtil. format(editorPart);

	}


	@Override
	public void setActiveEditor(IAction action, IEditorPart part) {

	}

}
