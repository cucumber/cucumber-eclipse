package cucumber.eclipse.editor.editors.jumpto;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import cucumber.eclipse.steps.integration.Step;

public class StepHyperlink implements IHyperlink {

	private IRegion region;
	private Step stepDefintion;

	public StepHyperlink(IRegion region, Step step) {
		this.region = region;
		this.stepDefintion = step;
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
		JumpToStepDefinition.openEditor(stepDefintion);
	}

}
