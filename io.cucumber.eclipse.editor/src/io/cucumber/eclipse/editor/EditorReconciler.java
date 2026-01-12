package io.cucumber.eclipse.editor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utility class to trigger reconciliation in open editors.
 * This is needed when preferences change to ensure code minings and other
 * features are updated immediately without requiring user edits.
 */
public class EditorReconciler {

	private static final String FEATURE_CONTENT_TYPE = "io.cucumber.eclipse.editor.content-type.feature";

	/**
	 * Triggers reconciliation for all open feature file editors.
	 * This forces code minings and other editor features to refresh.
	 */
	public static void reconcileAllFeatureEditors() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return;
		}
		
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorRef : page.getEditorReferences()) {
					IEditorPart editor = editorRef.getEditor(false);
					if (editor instanceof ITextEditor textEditor) {
						if (isFeatureEditor(textEditor)) {
							reconcileEditor(textEditor);
						}
					}
				}
			}
		}
	}
	
	public static void reconcileFeatureEditor(IDocument document) {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return;
		}
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorRef : page.getEditorReferences()) {
					IEditorPart editor = editorRef.getEditor(false);
					if (editor instanceof ITextEditor textEditor) {
						IDocumentProvider documentProvider = textEditor.getDocumentProvider();
						if (documentProvider != null
								&& documentProvider.getDocument(editor.getEditorInput()) == document) {
							reconcileEditor(textEditor);
							return;
						}
					}
				}
			}
		}
	}

	private static boolean isFeatureEditor(ITextEditor editor) {
		try {
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null) {
				IDocument document = provider.getDocument(editor.getEditorInput());
				if (document != null) {
					// Check if document has feature content type
					ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
					if (buffer != null) {
						IContentType contentType = buffer.getContentType();
						if (contentType != null && FEATURE_CONTENT_TYPE.equals(contentType.getId())) {
							return true;
						}
					}
				}
			}
		} catch (CoreException e) {
			// Ignore errors during editor detection
		}
		return false;
	}

	private static void reconcileEditor(ITextEditor editor) {
		try {
			// Get the source viewer through the proper adapter mechanism
			// AbstractTextEditor supports ITextViewer.class adapter since Eclipse 3.5
			Object adapter = editor.getAdapter(ITextViewer.class);
			if (adapter instanceof ISourceViewerExtension5 viewer) {
				viewer.updateCodeMinings();
			} else if (adapter == null) {
				// Fallback: try ITextOperationTarget which is also supported
				// and sometimes works when ITextViewer doesn't
				Object operationTarget = editor.getAdapter(ITextOperationTarget.class);
				if (operationTarget instanceof ISourceViewerExtension5 viewer) {
					viewer.updateCodeMinings();
				}
			}
		} catch (Exception e) {
			// Ignore errors during reconciliation trigger
		}
	}

}
