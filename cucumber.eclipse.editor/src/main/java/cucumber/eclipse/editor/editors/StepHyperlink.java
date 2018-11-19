package cucumber.eclipse.editor.editors;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import cucumber.eclipse.steps.integration.Step;

public class StepHyperlink implements IHyperlink {

	private IRegion region;
	private Step step;

	public StepHyperlink(IRegion region, Step step) {
		this.region = region;
		this.step = step;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	@Override
	public String getHyperlinkText() {
		return "Open step definition";
	}

	@Override
	public String getTypeLabel() {
		return "Gherkin step";
	}

	@Override
	public void open() {
		IResource file = this.step.getSource();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put(IMarker.LINE_NUMBER, step.getLineNumber());
		IMarker marker;
		try {
			marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
