package io.cucumber.eclipse.editor.hyperlinks;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import io.cucumber.eclipse.editor.CucumberServiceRegistry;
import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.editor.marker.MarkerFactory;

public class StepHyperlinkDetector implements IHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		int offset = region.getOffset();
		IDocument document = textViewer.getDocument();
		GherkinEditorDocument editorDocument = GherkinEditorDocumentManager.get(document);
		if (editorDocument != null) {
			IResource resource = editorDocument.getResource();
			List<IStepDefinitionOpener> openers = CucumberServiceRegistry.getStepDefinitionOpener().stream()
					.filter(opener -> {
						try {
							return opener.canOpen(resource);
						} catch (CoreException e1) {
							return false;
						}
					}).collect(Collectors.toList());
			if (openers.size() > 0) {
				try {
					int lineNumber = document.getLineOfOffset(offset) + 1;
					IRegion lineInfo = document.getLineInformationOfOffset(offset);
					int lineStartOffset = lineInfo.getOffset();
					if (MarkerFactory.hasMarker(editorDocument.getResource(), MarkerFactory.UNMATCHED_STEP,
							lineNumber)) {
						return null;
					}

					IHyperlink[] hyperlinks = editorDocument.getSteps()
							.filter(step -> step.getLocation().getLine() == lineNumber).map(step -> {
								long column = step.getLocation().getColumn().orElse(0l) - 1;
								String keyword = step.getKeyword();
								Long statementStartOffset = lineStartOffset + column + keyword.length();
								IRegion stepRegion = new Region(statementStartOffset.intValue(), step.getText().length());

								return new StepHyperlink(stepRegion, step, textViewer, resource, openers);
							}).filter(Objects::nonNull).toArray(IHyperlink[]::new);
					if (hyperlinks.length > 0) {
						return hyperlinks;
					}
				} catch (BadLocationException e) {
					EditorLogging.error("Failed to detect hyperlink at offset: " + offset, e);
				} catch (CoreException e) {
					EditorLogging.error("Failed to detect hyperlink", e);
				}
			}
		}
		return null;
	}

}
