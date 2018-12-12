package cucumber.eclipse.editor.editors;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import cucumber.eclipse.editor.util.ExtensionRegistryUtil;
import cucumber.eclipse.steps.integration.IStepDefinitions;

public class RecalculateStepsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// TODO should launch an incremental build on the refreshed resources 
		
		
		// rebuild for the selected element
		// then refresh 
		
		// TODO recalculate steps from a feature file with the builder
		
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);

		// Needs to be a gherkin editor for this to work, if not then simply do nothing.
		if (!(editorPart instanceof Editor)) {
			return null;
		}
		
		Editor editor = (Editor) editorPart;
		
		IFile file = editor.getFile();
		try {
			file.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//editor.refresh();
		//editor.get
		
		
		return null;
	}

}
