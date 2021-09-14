package io.cucumber.eclipse.java.quickfix;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import io.cucumber.eclipse.editor.Images;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.validation.CucumberGlueValidator;

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
				IJavaProject project = JDTUtil.getJavaProject(resource);
				if (project != null) {
					GherkinEditorDocument editorDocument = GherkinEditorDocument.get(resource);
					if (editorDocument != null) {
						Collection<ICompilationUnit> glueSources = JDTUtil.getGlueSources(project, null);
						// TODO sort?, show sources that are used in the document first?
						return glueSources.stream().filter(unit -> unit.getResource() instanceof IFile)
								.sorted((c1, c2) -> c1.getElementName().compareToIgnoreCase(c2.getElementName()))
								.map(unit -> new StepCreationMarkerResolution(unit, editorDocument))
								.toArray(IMarkerResolution[]::new);

					}
				}
			}
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

	private static class StepCreationMarkerResolution extends WorkbenchMarkerResolution implements IMarkerResolution {
		private ICompilationUnit unit;
		private GherkinEditorDocument editorDocument;

		public StepCreationMarkerResolution(ICompilationUnit unit, GherkinEditorDocument editorDocument) {
			this.unit = unit;
			this.editorDocument = editorDocument;
		}

		@Override
		public void run(IMarker marker) {
			String snippet = marker.getAttribute(MarkerFactory.UNMATCHED_STEP_SNIPPET_ATTRIBUTE, "");
			applySnippet(snippet);
		}

		private void applySnippet(String snippet) {
			try {
				SnippetApplicator.generateSnippet(snippet, (IFile) unit.getResource());
				CucumberGlueValidator.revalidate(editorDocument.getDocument());
			} catch (CoreException | MalformedTreeException | IOException | BadLocationException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						String.format("Couldn't generate snippet %s for %s", snippet, unit.getResource()), e));
			}
		}

		@Override
		public void run(IMarker[] markers, IProgressMonitor monitor) {
			// TODO apply as one big edit
			SubMonitor subMonitor = SubMonitor.convert(monitor, markers.length);
			Set<String> created = new HashSet<>();
			for (IMarker marker : markers) {
				String snippet = marker.getAttribute(MarkerFactory.UNMATCHED_STEP_SNIPPET_ATTRIBUTE, "");
				if (created.add(snippet)) {
					applySnippet(snippet);
				}
				subMonitor.worked(1);
			}
		}

		@Override
		public String getLabel() {
			return String.format("Create step in %s", unit.getElementName());
		}

		@Override
		public String getDescription() {
			return "Create missing step in existing java source file";
		}

		@Override
		public Image getImage() {
			return Images.getCukesIcon();
		}

		@Override
		public IMarker[] findOtherMarkers(IMarker[] markers) {
			return Arrays.stream(markers).filter(marker -> {
				try {
					return MarkerFactory.UNMATCHED_STEP.equals(marker.getType());
				} catch (CoreException e) {
					return false;
				}
			}).toArray(IMarker[]::new);
		}

	}
}
