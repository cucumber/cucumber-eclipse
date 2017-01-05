package cucumber.eclipse.editor.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

/**
 * @author andreas
 *
 */
public class GherkinReconcilingStrategy implements IReconcilingStrategy,
		IReconcilingStrategyExtension {
	private IDocument document;
	private Editor editor;

	public GherkinReconcilingStrategy(Editor editor) {
		this.editor = editor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#
	 * setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#
	 * initialReconcile()
	 */
	@Override
	public void initialReconcile() {
		editor.getModel().updateFromDocument(document);
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateGherkinModel(editor.getModel());
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org
	 * .eclipse.jface.text.IDocument)
	 */
	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.
	 * eclipse.jface.text.reconciler.DirtyRegion,
	 * org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.
	 * eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(IRegion partition) {
		initialReconcile();
	}

}
