package cucumber.eclipse.editor.editors;

import gherkin.parser.ParseError;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import cucumber.eclipse.editor.Activator;



public class GherkinPopupMenuPrettyFormatActionDelegate implements
		IObjectActionDelegate {

	private Shell shell;
	private IFile docSelected;
	
	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		if (arg1 instanceof TreeSelection) {
			TreeSelection tree = (TreeSelection) arg1;
			docSelected = (IFile) tree.getFirstElement();
		}

	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();

	}

	@Override
	public void run(IAction arg0) {
		
		IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        ITextEditor editor = (ITextEditor) editorPart;
        IDocumentProvider dp = editor.getDocumentProvider();
        IDocument doc = dp.getDocument(editor.getEditorInput());
        
        String title = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePartReference().getTitle();
        if (title.equals(docSelected.getName())) {
            String contents = doc.get();
            
    		String formatted;
    		try {
    			formatted = GherkinFormatterUtil.format(contents);
    			doc.replace(0, doc.getLength(), formatted);
    		} catch (ParseError e) {
    			MessageDialog.openInformation(shell, "Unable to pretty format.", 
    					"One can only format a feature file that has no parse errors: \n"
    					+ "The following parse error was encountered: [" + e.getMessage() +"]");
    			
    		} catch (BadLocationException e) {
    			e.printStackTrace();
    		}

        } else {
        	MessageDialog.openInformation(shell, "This is still Alpha...", 
					"One can only format a feature file from the popup menu if... \n"
					+ ".. it is the active editor... sorry.");
        }
        
   

		
	}

}
