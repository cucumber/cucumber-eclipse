package cucumber.eclipse.editor.markers;

import static cucumber.eclipse.steps.integration.marker.MarkerFactory.NOT_A_CUCUMBER_PROJECT;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import cucumber.eclipse.editor.i18n.CucumberEditorMessages;
import cucumber.eclipse.editor.nature.CucumberProjectNature;

/**
 * Quick fix to configure a project when the editor detected
 * a gherkin source file from a project without the cucumber nature.
 * 
 * @author qvdk
 *
 */
public class ConfigureAsCucumberProjectMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	/**
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {

		if (!hasResolutions(marker)) {
			return new IMarkerResolution[0];
		}

		return new IMarkerResolution[] { new ConfigureAsCucumberProjectMarkerResolution(marker.getResource().getProject()) };
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		boolean hasResolutions = false;
		try {
			hasResolutions = NOT_A_CUCUMBER_PROJECT.equals(marker.getType());
		} catch (CoreException e) {
			e.printStackTrace();
			hasResolutions = false;
		}
		return hasResolutions;
	}

	private class ConfigureAsCucumberProjectMarkerResolution implements IMarkerResolution {

		private IProject project;
		
		public ConfigureAsCucumberProjectMarkerResolution(IProject project) {
			super();
			this.project = project;
		}

		@Override
		public String getLabel() {
			return NLS.bind(CucumberEditorMessages.MarkerResolution__Configure_as_cucumber_project, project.getName());
		}

		@Override
		public void run(IMarker marker) {
			try {
				CucumberProjectNature cucumberProjectNature = new CucumberProjectNature();
				cucumberProjectNature.setProject(project);
				cucumberProjectNature.configure();
				
				marker.delete();
				
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
	}
}
