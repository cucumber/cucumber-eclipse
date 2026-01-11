package io.cucumber.eclipse.editor.format;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;

public class EditorInputPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IEditorPart part) {
			if ("isGherkingDocument".equals(property)) {
				IEditorInput editorInput = part.getEditorInput();
				if (editorInput instanceof IFileEditorInput fileInput) {
					ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager()
							.getTextFileBuffer(fileInput.getFile().getFullPath(), LocationKind.IFILE);
					if (buffer != null) {
						return GherkinEditorDocumentManager.isCompatible(buffer.getDocument());
					}
				}
			}
		}
		return false;
	}

}
