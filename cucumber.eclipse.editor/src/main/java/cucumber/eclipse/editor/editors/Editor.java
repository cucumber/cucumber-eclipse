package cucumber.eclipse.editor.editors;

import org.eclipse.ui.editors.text.TextEditor;


public class Editor extends TextEditor {

	private ColorManager colorManager;
	public Editor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new GherkinConfiguration(colorManager));
		setDocumentProvider(new GherkinDocumentProvider());
	}
	public void dispose() {
		super.dispose();
		colorManager.dispose();
	}

}
