package cucumber.eclipse.editor.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.editor.Activator;

public class GherkinPopupMenuFeatureRunnerActionDelegate implements IEditorActionDelegate {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void run(IAction action) {

		IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextEditor part = (ITextEditor) editorPart;
		}

	@Override
	public void setActiveEditor(IAction action, IEditorPart part) {

	}

}
