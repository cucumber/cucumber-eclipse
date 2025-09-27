package io.cucumber.eclipse.editor.format;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.gherkin.utils.pretty.Pretty;
import io.cucumber.gherkin.utils.pretty.Syntax;
import io.cucumber.messages.types.GherkinDocument;

public class GherkingFormatHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (part instanceof IEditorPart editor) {
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof IFileEditorInput fileInput) {
				GherkinEditorDocument document = GherkinEditorDocument.get(fileInput.getFile());
				if (document != null) {
					formatDocument(document, fileInput.getFile());
				}
			}
		}
		return null;
	}

	private void formatDocument(GherkinEditorDocument document, IFile file) {
		Optional<GherkinDocument> gherkinDocument = document.getGherkinDocument();
		if (gherkinDocument.isEmpty()) {
			return;
		}
		String prettyPrint = Pretty.prettyPrint(gherkinDocument.get(), Syntax.gherkin);
		document.getDocument().set(prettyPrint);
	}
}
