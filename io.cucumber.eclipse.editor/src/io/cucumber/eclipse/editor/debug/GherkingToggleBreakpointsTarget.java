package io.cucumber.eclipse.editor.debug;

import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.Location;

/**
 * Service for providing breakpoints to the generic editor
 * 
 * @author christoph
 *
 */
@Component(service = IToggleBreakpointsTarget.class)
public class GherkingToggleBreakpointsTarget implements IToggleBreakpointsTargetExtension {

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		toggleBreakpoints(part, selection);
	}

	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return canToggleBreakpoints(part, selection);
	}

	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// not supported
	}

	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// not supported
	}

	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	@Override
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ITextViewer textViewer = Adapters.adapt(part, ITextViewer.class);
		if (textViewer != null) {
			GherkinEditorDocument gherkinEditorDocument = GherkinEditorDocument.get(textViewer.getDocument());
			if (gherkinEditorDocument != null) {
				ITextEditor textEditor = Adapters.adapt(part, ITextEditor.class);
				if (textEditor != null) {
					IResource resource = textEditor.getEditorInput().getAdapter(IResource.class);
					ITextSelection textSelection = (ITextSelection) selection;
					int lineNumber = textSelection.getStartLine();
					IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
							.getBreakpoints(GherkingBreakpoint.MODEL_ID);
					for (IBreakpoint breakpoint : breakpoints) {
						if (breakpoint instanceof ILineBreakpoint) {
							if (resource.equals(breakpoint.getMarker().getResource())) {
								if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
									breakpoint.delete();
									return;
								}
							}
						}
					}
					GherkingBreakpoint lineBreakpoint = new GherkingBreakpoint(resource, lineNumber + 1);
					DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
				}
			}
		}
	}

	@Override
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof ITextSelection) {
			ITextViewer textViewer = Adapters.adapt(part, ITextViewer.class);
			if (textViewer != null) {
				IDocument document = textViewer.getDocument();
				if (document != null) {
					GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
					ITextSelection textSelection = (ITextSelection) selection;
					int lineNumber = textSelection.getStartLine() + 1;
					Stream<Location> stream = editorDocument.getSteps().map(Step::getLocation);
					return stream.mapToInt(Location::getLine).filter(line -> line == lineNumber).findAny().isPresent();
				}
			}
		}
		return false;
	}

}
