package cucumber.eclipse.editor.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.nature.CucumberProjectNature;
import cucumber.eclipse.editor.template.GherkinSampleTemplate;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public class Editor extends TextEditor {

	private ColorManager colorManager;
	private ProjectionSupport projectionSupport;
	private ProjectionAnnotationModel annotationModel;
	private Annotation[] oldAnnotations;
	private GherkinOutlinePage outlinePage;
	private GherkinModel model;

	public Editor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new GherkinConfiguration(this, colorManager));

		// Commented By Girija to override any blank feature file with sample template
		// setDocumentProvider(new GherkinDocumentProvider());

		// Added By Girija
		// Used to create a Sample Template for any Blank Feature File
		setDocumentProvider(new GherkinDocumentProvider(GherkinSampleTemplate.getFeatureTemplate()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
				styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();

		// turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = viewer.getProjectionAnnotationModel();

		// register the editor scope context
		IContextService service = (IContextService) getSite().getService(IContextService.class);
		if (service != null) {
			service.activateContext("cucumber.eclipse.editor.featureEditorScope");
		}
	}

	public GherkinModel getModel() {
		return model;
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

	TextSelection getSelection() {
		return (TextSelection) getSelectionProvider().getSelection();
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
		model = new GherkinModel();
		this.warnIfFeatureFileOpenedComesFromNonCucumberProject();
	}

	private void warnIfFeatureFileOpenedComesFromNonCucumberProject() throws CoreException {
		IFile featureFile = this.getFile();
		IProject projectWithGherkinFile = featureFile.getProject();
		boolean isCucumberProject = projectWithGherkinFile.hasNature(CucumberProjectNature.ID);
		if(!isCucumberProject) {
			MarkerFactory markerFactory = MarkerFactory.INSTANCE;
			markerFactory.featureFileIsNotInCucumberProject(featureFile);
		}
	}
	
	
	public IFile getFile() {
 		IFileEditorInput fileEditorInput = (IFileEditorInput) this.getEditorInput();
		return fileEditorInput.getFile();
	}

	public void dispose() {
		super.dispose();

		colorManager.dispose();

	}

	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (outlinePage == null) {
				outlinePage = new GherkinOutlinePage();
				outlinePage.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						PositionedElement firstElement = (PositionedElement) ((IStructuredSelection) event
								.getSelection()).getFirstElement();

						if (firstElement != null) {
							try {
								selectAndReveal(firstElement.toPosition().getOffset(), 0);
							} catch (BadLocationException e) {
								Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										"Couldn't set editor selection " + "from outline", e));
							}
						}
					}
				});
			}
			outlinePage.setInput(getEditorInput());
			return outlinePage;
		}
		return super.getAdapter(required);
	}

}
