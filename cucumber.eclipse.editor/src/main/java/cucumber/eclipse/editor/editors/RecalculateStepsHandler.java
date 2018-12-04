package cucumber.eclipse.editor.editors;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import cucumber.eclipse.editor.util.ExtensionRegistryUtil;
import cucumber.eclipse.steps.integration.IStepDefinitions;

public class RecalculateStepsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);

		// Needs to be a gherkin editor for this to work, if not then simply do nothing.
		if (!(editorPart instanceof Editor)) {
			return null;
		}

		Editor editor = (Editor) editorPart;
		
		editor.refresh();

		return null;
	}

}
