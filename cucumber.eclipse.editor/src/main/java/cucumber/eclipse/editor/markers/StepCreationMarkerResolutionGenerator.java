package cucumber.eclipse.editor.markers;

import static cucumber.eclipse.editor.editors.DocumentUtil.read;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.editors.Editor;
import cucumber.eclipse.editor.editors.GherkinModel;
import cucumber.eclipse.editor.editors.PositionedElement;
import cucumber.eclipse.editor.snippet.ExtensionRegistryStepGeneratorProvider;
import cucumber.eclipse.editor.snippet.IStepGeneratorProvider;
import cucumber.eclipse.editor.snippet.SnippetApplicator;
import cucumber.eclipse.editor.steps.ExtensionRegistryStepProvider;
import cucumber.eclipse.steps.integration.Step;

public class StepCreationMarkerResolutionGenerator implements IMarkerResolutionGenerator {
	
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		
		Set<IFile> files = new HashSet<IFile>();
		
		ExtensionRegistryStepProvider prof = new ExtensionRegistryStepProvider((IFile) marker.getResource());
		Set<Step> steps;
		try {
			steps = prof.getSteps(null);
		} catch (CoreException e) {
			e.printStackTrace();
			return new IMarkerResolution[0];
		}
		
		for (Step step : steps) {
			files.add((IFile) step.getSource());
		}
		
		List<IFile> filesList = new ArrayList<IFile>(files);
		Collections.sort(filesList, new Comparator<IFile>() {
			@Override
			public int compare(IFile o1, IFile o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		IMarkerResolution[] resolutions = new IMarkerResolution[filesList.size()];
		
		for (int i = 0; i < resolutions.length; i ++) {
			resolutions[i] = new StepCreationMarkerResolution(filesList.get(i));
		}
		
		return resolutions;
	}

	private static class StepCreationMarkerResolution implements IMarkerResolution {
				
		private final IFile stepFile;
		
		public StepCreationMarkerResolution(IFile stepFile) {
			this.stepFile = stepFile;
		}
		
		@Override
		public void run(IMarker marker) {
			IFile featureFile = ((IFile) marker.getResource());
			IStepGeneratorProvider generatorProvider = new ExtensionRegistryStepGeneratorProvider();
		
			try {
				GherkinModel model = getCurrentModel(featureFile);
				PositionedElement element = model.getStepElement(marker.getAttribute(IMarker.CHAR_START, 0));
				
				gherkin.formatter.model.Step step = ((gherkin.formatter.model.Step) element.getStatement());
				new SnippetApplicator(generatorProvider).generateSnippet(step, stepFile);
			}
			catch (IOException exception) {
				logException(marker, exception);
			}
			catch (CoreException exception) {
				logException(marker, exception);
			}
			catch (BadLocationException exception) {
				logException(marker, exception);
			}
		}
		
		@Override
		public String getLabel() {
			return String.format("Create step in %s", stepFile.getName());
		}

		private static GherkinModel getCurrentModel(IFile featureFile) throws IOException, CoreException {
			GherkinModel model = getModelFromOpenEditor(featureFile);
			
			if (model == null) {
				model = getModelFromFile(featureFile);
			}
			
			return model;
		}

		private static GherkinModel getModelFromOpenEditor(IFile featureFile) throws PartInitException {
			IEditorReference[] editorReferences = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			
			for (IEditorReference editorReference : editorReferences) {
				if (editorReference.getEditorInput() instanceof FileEditorInput) {
					FileEditorInput fileEditorInput = (FileEditorInput) editorReference.getEditorInput();
					
					if (featureFile.equals(fileEditorInput.getFile())) {
						IEditorPart editor = editorReference.getEditor(false);

						if (editor instanceof Editor) {
							return ((Editor) editor).getModel();
						}
					}
				}
			}
			
			return null;
		}

		private static GherkinModel getModelFromFile(IFile featureFile) throws IOException, CoreException {
			GherkinModel model = new GherkinModel();
			model.updateFromDocument(read(featureFile.getContents(), featureFile.getCharset()));
			return model;
		}
		
		private static void logException(IMarker marker, Exception exception) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				String.format("Couldn't create step for %s", marker), exception));
		}
	}
}
