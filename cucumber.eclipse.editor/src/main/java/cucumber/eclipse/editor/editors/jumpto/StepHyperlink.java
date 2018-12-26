package cucumber.eclipse.editor.editors.jumpto;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import cucumber.eclipse.steps.integration.StepDefinition;

public class StepHyperlink implements IHyperlink {

	private IRegion region;
	private StepDefinition stepDefintion;

	public StepHyperlink(IRegion region, StepDefinition stepDefinition) {
		this.region = region;
		this.stepDefintion = stepDefinition;
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
