package cucumber.eclipse.editor.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;


public class Editor extends TextEditor {

	private ColorManager colorManager;
	public Editor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new GherkinConfiguration(this, colorManager));
		setDocumentProvider(new GherkinDocumentProvider());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	private ProjectionSupport projectionSupport;
	private ProjectionAnnotationModel annotationModel;
	private Annotation[] oldAnnotations;
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(viewer,
				getAnnotationAccess(), getSharedColors());
		projectionSupport.install();

		// turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = viewer.getProjectionAnnotationModel();
	}

	public void updateFoldingStructure( List<Position> positions )
	{
		Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>();
		for( Position p : positions ) {
			newAnnotations.put( new ProjectionAnnotation(), p );
		}
		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = newAnnotations.keySet().toArray( new Annotation[0] );
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId("#CukeEditorContext");
	}



	public void dispose() {
		super.dispose();
		colorManager.dispose();
	}

}
