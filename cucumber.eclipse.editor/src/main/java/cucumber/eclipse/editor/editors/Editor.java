package cucumber.eclipse.editor.editors;

import gherkin.lexer.LexingError;
import gherkin.parser.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import cucumber.eclipse.editor.markers.MarkerManager;
import cucumber.eclipse.editor.steps.ExtensionRegistryStepProvider;

public class Editor extends TextEditor {

	private ColorManager colorManager;
	private IEditorInput input;

	public Editor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new GherkinConfiguration(this,
				colorManager));
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
	private GherkinOutlinePage outlinePage;
	
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

		// register the editor scope context
		IContextService service = (IContextService) getSite().getService(
				IContextService.class);
		if (service != null) {
			service.activateContext("cucumber.eclipse.editor.featureEditorScope");
		}
	}
	
	public void updateGherkinModel(GherkinModel model) {
		updateOutline(model.getFeatureElement());
		updateFoldingStructure(model.getFoldRanges());
	}

	public void updateFoldingStructure(List<Position> positions) {
		Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>();
		for (Position p : positions) {
			newAnnotations.put(new ProjectionAnnotation(), p);
		}
		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = newAnnotations.keySet().toArray(new Annotation[0]);
	}

	private void updateOutline(PositionedElement featureElement) {
		if (outlinePage != null) {
			outlinePage.update(featureElement);
		}
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId("#CukeEditorContext");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.editors.text.TextEditor#doSetInput(org.eclipse.ui.IEditorInput
	 * )
	 */
	@Override
	protected void doSetInput(IEditorInput newInput) throws CoreException {
		super.doSetInput(newInput);
		input = newInput;
		validateAndMark();
	}

	public void dispose() {
		super.dispose();
		colorManager.dispose();
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();
		validateAndMark();
	}

	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (outlinePage == null) {
				outlinePage = new GherkinOutlinePage();
			}
			outlinePage.setInput(getEditorInput());
			return outlinePage;
		}
		return super.getAdapter(required);
	}

	private void validateAndMark() {
		IDocument doc = getDocumentProvider().getDocument(input);
		IFileEditorInput fileEditorInput = (IFileEditorInput) input;
		IFile featureFile = fileEditorInput.getFile();
		GherkinErrorMarker marker = new GherkinErrorMarker(new ExtensionRegistryStepProvider(),
				new MarkerManager(), featureFile,
				doc);
		marker.removeExistingMarkers();

		Parser p = new Parser(marker, false);
		try {
			p.parse(doc.get(), "", 0);
		} catch (LexingError l) {
		}
	}
}
