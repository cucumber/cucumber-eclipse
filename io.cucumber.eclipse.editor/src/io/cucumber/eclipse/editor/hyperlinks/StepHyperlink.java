package io.cucumber.eclipse.editor.hyperlinks;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.messages.types.Step;

public class StepHyperlink implements IHyperlink {

	private IRegion region;
	private Step stepDefintion;
	private ITextViewer textViewer;
	private Collection<IStepDefinitionOpener> openers;
	private IResource resource;

	public StepHyperlink(IRegion region, Step step, ITextViewer textViewer, IResource resource,
			Collection<IStepDefinitionOpener> openers) {
		this.region = region;
		this.stepDefintion = step;
		this.textViewer = textViewer;
		this.resource = resource;
		this.openers = openers;
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
		for (IStepDefinitionOpener opener : openers) {
			try {
				if (opener.openInEditor(textViewer, resource, stepDefintion)) {
					return;
				}
			} catch (CoreException e) {
				EditorLogging.error("Failed to open step definition in editor", e);
			}
		}
		MessageDialog.openInformation(textViewer.getTextWidget().getShell(), getTypeLabel(),
				"No source found for step " + stepDefintion.getText());
	}

}
