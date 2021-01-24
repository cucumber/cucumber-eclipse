package io.cucumber.eclipse.editor.marker;

import static io.cucumber.eclipse.editor.marker.MarkerFactory.UNMATCHED_STEP_KEYWORD_ATTRIBUTE;
import static io.cucumber.eclipse.editor.marker.MarkerFactory.UNMATCHED_STEP_NAME_ATTRIBUTE;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class StepCreationMarkerResolutionGenerator implements IMarkerResolutionGenerator {
	
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
			
			if(!isUnmatchedStepMarker) {
				return new IMarkerResolution[0];
			}
			
			String gherkinStepKeyword = (String) marker.getAttribute(UNMATCHED_STEP_KEYWORD_ATTRIBUTE);
			String gherkinStepName = (String) marker.getAttribute(UNMATCHED_STEP_NAME_ATTRIBUTE);
			
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

//	private static class StepCreationMarkerResolution implements IMarkerResolution {
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
//		public void run(IMarker marker) {
//			IStepGeneratorProvider generatorProvider = new ExtensionRegistryStepGeneratorProvider();
//		
//			new SnippetApplicator(generatorProvider).generateSnippet(gherkinStep, stepDefinitionsFile);
//			
//		}
//		
//		@Override
//		public String getLabel() {
//			return String.format("Create step in %s", stepDefinitionsFile.getName());
//		}
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
//	}
}
