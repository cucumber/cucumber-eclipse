package io.cucumber.eclipse.java.quickfix;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class SnippetApplicator {

	
	
	public static void generateSnippet(String snippet, IFile stepFile)
			throws IOException, CoreException, MalformedTreeException, BadLocationException {
		// TODO allow to define additional formating e.g. javadoc
		ITextEditor editor = openEditor(stepFile);
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TextEdit textEdit = StepGenerator.createStepSnippet(snippet, document);
		textEdit.apply(document);
		editor.selectAndReveal(textEdit.getRegion().getOffset(), textEdit.getRegion().getLength());
	}

	private static ITextEditor openEditor(IFile stepFile) throws PartInitException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(stepFile.getName());
		
		ITextEditor editor = (ITextEditor) page.openEditor(new FileEditorInput(stepFile), desc.getId());
		return editor;
	}
}

