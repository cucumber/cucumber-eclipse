package cucumber.eclipse.editor.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class GherkinFeatureRunnerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = (IEditorPart)HandlerUtil.getActiveEditorChecked(event);
		GherkinFormatterUtil.format(editorPart);
		return null;
	}

}
