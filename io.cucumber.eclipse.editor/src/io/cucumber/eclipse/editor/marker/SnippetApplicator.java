package io.cucumber.eclipse.editor.marker;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class SnippetApplicator {

	
	private String snipet;

	public SnippetApplicator(String snipet) {
		this.snipet = snipet;
	}
	
//	public void generateSnippet(Step step, IFile stepFile) {
//		try {
//			IStepDefinitionGenerator generator = generatorProvider.getStepGenerator(stepFile);
//			
//			ITextEditor editor = openEditor(stepFile);
//			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
//			
//			TextEdit edit = generator.createStepSnippet(step, document);
//
//			edit.apply(document);
//			
//			editor.selectAndReveal(edit.getRegion().getOffset(), edit.getRegion().getLength());
//		}
//		catch (PartInitException exception) {
//			logException(step, stepFile, exception);
//		}
//		catch (BadLocationException exception) {
//			logException(step, stepFile, exception);
//		}
//		catch (IOException exception) {
//			logException(step, stepFile, exception);
//		}
//		catch (CoreException exception) {
//			logException(step, stepFile, exception);
//		}
//	}
//
//	private static void logException(Step step, IFile stepFile, Exception exception) {
//		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
//			String.format("Couldn't generate snippet %s for %s", step.getName(), stepFile), exception));
//	}

	private static ITextEditor openEditor(IFile stepFile) throws PartInitException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(stepFile.getName());
		
		ITextEditor editor = (ITextEditor) page.openEditor(new FileEditorInput(stepFile), desc.getId());
		return editor;
	}
}

