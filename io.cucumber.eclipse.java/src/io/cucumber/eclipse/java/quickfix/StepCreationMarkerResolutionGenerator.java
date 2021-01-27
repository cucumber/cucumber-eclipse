package io.cucumber.eclipse.java.quickfix;

import java.util.Collection;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;

// TODO instead of existing files we should consider allow to create a new file with the snippet
public class StepCreationMarkerResolutionGenerator implements IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	/**
	 * Return a list of suggested resolutions for a gherkin step without step
	 * definitions.
	 * 
	 * The plugin suggests to create the skeleton of a gherkin step in known step
	 * definitions file.
	 * 
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		try {
			boolean isUnmatchedStepMarker = MarkerFactory.UNMATCHED_STEP.equals(marker.getType());

			if (!isUnmatchedStepMarker) {
				return new IMarkerResolution[0];
			}

			if (hasResolutions(marker)) {
				IResource resource = marker.getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					// FIXME instead of static use Adapters.adapt();
					ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager()
							.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
					IJavaProject project = JDTUtil.getJavaProject(resource);
					if (buffer != null && project != null) {

						Collection<ICompilationUnit> glueSources = JDTUtil.getGlueSources(project, null);
						// TODO sort, show sources that are used in the document first
						return glueSources.stream()
								.sorted((c1, c2) -> c1.getElementName().compareToIgnoreCase(c2.getElementName()))
								.map(unit -> new StepCreationMarkerResolution(unit, buffer))
								.toArray(IMarkerResolution[]::new);
					}

				}
			}

//			Step gherkinStep = new Step(null, gherkinStepKeyword, gherkinStepName, null, null, null);
//			
//			IFile gherkinFile = (IFile) marker.getResource();
//			IProject project = gherkinFile.getProject();
//			
//			UniversalStepDefinitionsProvider stepProvider = UniversalStepDefinitionsProvider.INSTANCE;
//			if(!stepProvider.isInitialized(project)) {
//				try {
//					stepProvider.load(project);
//				} catch (CoreException e) {
//					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//					ErrorDialog.openError(shell, "Any cucumber build results found.", "Build the project to get resolution suggestions.", e.getStatus());
//				}
//			}
//			
//			Set<IFile> stepDefinitionsFiles = stepProvider.getStepDefinitionsFiles(project);
//			
//			IMarkerResolution[] resolutions = new IMarkerResolution[stepDefinitionsFiles.size()];
//			int it = 0;
//			for (IFile stepDefinitionsFile : stepDefinitionsFiles) {
//				resolutions[it++] = new StepCreationMarkerResolution(gherkinStep, stepDefinitionsFile);
//			}			
//			
//			return resolutions;
			return new IMarkerResolution[0];
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return new IMarkerResolution[0];
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		String snippet = marker.getAttribute(MarkerFactory.UNMATCHED_STEP_SNIPPET_ATTRIBUTE, "");
		return !snippet.isBlank()
				&& Activator.PLUGIN_ID
						.equals(marker.getAttribute(MarkerFactory.UNMATCHED_STEP_SNIPPTE_TYPE_ATTRIBUTE, ""))
				&& JDTUtil.isJavaProject(marker.getResource().getProject());
	}

	private static class StepCreationMarkerResolution
			/* TODO extends WorkbenchMarkerResolution */ implements IMarkerResolution {
		private ICompilationUnit unit;
		private ITextFileBuffer textFileBuffer;

		public StepCreationMarkerResolution(ICompilationUnit unit, ITextFileBuffer buffer) {
			this.unit = unit;
			this.textFileBuffer = buffer;
			// TODO Auto-generated constructor stub
		}

		//
//		private final IFile stepDefinitionsFile;
//		private final Step gherkinStep;
//		
//		public StepCreationMarkerResolution(Step gherkinStep, IFile stepDefinitionsFile) {
//			this.stepDefinitionsFile = stepDefinitionsFile;
//			this.gherkinStep = gherkinStep;
//		}
//		
//		@Override
		@Override
		public void run(IMarker marker) {
//			IDocument document = buffer.getDocument();
//			IStepGeneratorProvider generatorProvider = new ExtensionRegistryStepGeneratorProvider();
//		
//			new SnippetApplicator(generatorProvider).generateSnippet(gherkinStep, stepDefinitionsFile);
//			
		}

//		
//		@Override
		@Override
		public String getLabel() {
			return String.format("Create step in %s", unit.getElementName());
		}
//
//		private static GherkinModel getCurrentModel(IFile featureFile) throws IOException, CoreException {
//			GherkinModel model = getModelFromOpenEditor(featureFile);
//			
//			if (model == null) {
//				model = getModelFromFile(featureFile);
//			}
//			
//			return model;
//		}
//
//		private static GherkinModel getModelFromOpenEditor(IFile featureFile) throws PartInitException {
//			IEditorReference[] editorReferences = PlatformUI.getWorkbench()
//					.getActiveWorkbenchWindow().getActivePage().getEditorReferences();
//			
//			for (IEditorReference editorReference : editorReferences) {
//				if (editorReference.getEditorInput() instanceof FileEditorInput) {
//					FileEditorInput fileEditorInput = (FileEditorInput) editorReference.getEditorInput();
//					
//					if (featureFile.equals(fileEditorInput.getFile())) {
//						IEditorPart editor = editorReference.getEditor(false);
//
//						if (editor instanceof Editor) {
//							return ((Editor) editor).getModel();
//						}
//					}
//				}
//			}
//			
//			return null;
//		}
//
//		private static GherkinModel getModelFromFile(IFile featureFile) throws IOException, CoreException {
//			GherkinModel model = new GherkinModel();
//			model.updateFromDocument(read(featureFile.getContents(), featureFile.getCharset()));
//			return model;
//		}
//		
//		private static void logException(IMarker marker, Exception exception) {
//			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
//				String.format("Couldn't create step for %s", marker), exception));
//		}

//		@Override
//		public String getDescription() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Image getImage() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public IMarker[] findOtherMarkers(IMarker[] markers) {
//			// TODO Auto-generated method stub
//			return null;
//		}
	}
}
