package cucumber.eclipse.editor.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;

public class MarkerResolutionProposal implements ICompletionProposal {

	private final IMarker marker;
	
	private final IMarkerResolution delegate;
	
	public MarkerResolutionProposal(IMarker marker, IMarkerResolution delegate) {
		this.marker = marker;
		this.delegate = delegate;
	}
	
	@Override
	public void apply(IDocument document) {
		delegate.run(marker);
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return delegate.getLabel();
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
}
