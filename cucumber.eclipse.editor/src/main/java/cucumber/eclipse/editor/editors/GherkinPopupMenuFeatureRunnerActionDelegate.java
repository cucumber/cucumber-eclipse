package cucumber.eclipse.editor.editors;


import gherkin.parser.ParseError;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
//import org.eclipse.ui.part.FileEditorInput;









import cucumber.api.cli.Main;
import cucumber.eclipse.editor.Activator;

public class GherkinPopupMenuFeatureRunnerActionDelegate implements
		IEditorActionDelegate {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void run(IAction action) {

		IEditorPart editorPart = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextEditor part = (ITextEditor) editorPart;
	//	IDocument input = part.getDocumentProvider().getDocument(part.getEditorInput());
//		part.
//		//System.out.println(doc.g);
//		Shell shell = editorPart.getSite().getShell();
//		
//		if (((IEditorPart) part).getEditorInput() instanceof IFileEditorInput) {
//            IFile file = ((IFileEditorInput) ((EditorPart) part)
//                    .getEditorInput()).getFile();
//            System.out.println(file.getLocation());
//        }
		
		if (((IEditorPart) part).getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) ((EditorPart) part)
                    .getEditorInput()).getFile();
            System.out.println(file.getLocation());
        
            String dir = System.getProperty("user.dir");
		System.out.println("user.dir:" + dir);
		String[] argv = new String[3];
		String arg1 = "--glue";
		String arg2 = "Users/ilanpillemer/Developer/Projects/cucumber-jvm/examples/java-calculator/src/test/java/cucumber/examples/java/calculator/";
		String arg3 = file.getLocation().toString();
		argv[0] = arg1;
		argv[1] = arg2;
		argv[2] = arg3;
		
		System.out.println(argv[0]);
		System.out.println(argv[1]);
		System.out.println(argv[2]);
		
		try {
			//cucumber.api.cli.Main main = new Main();
			cucumber.api.cli.Main.run(argv,this.getClass().getClassLoader());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		//input.g
		//GherkinFormatterUtil. format(editorPart);
		//IPath path =   ((IFileEditorInput)input).getFile().getFullPath();
		
		
		
		//MessageDialog.openInformation(shell, "DEBUG",path.toOSString());
//		IWorkbench workbench = PlatformUI.getWorkbench();
//		IWorkbenchWindow window = 
//		        workbench == null ? null : workbench.getActiveWorkbenchWindow();
//		IWorkbenchPage activePage = 
//		        window == null ? null : window.getActivePage();
//
//		IEditorPart editor = 
//		        activePage == null ? null : activePage.getActiveEditor();
//		IEditorInput input = 
//		        editor == null ? null : editor.getEditorInput();
//		IPath path = input instanceof FileEditorInput 
//		        ? ((FileEditorInput)input).getPath()
//		        : null;
//		if (path != null)
//		{
//		    // Do something with path.
//		}

	}


	@Override
	public void setActiveEditor(IAction action, IEditorPart part) {

	}

}
