package io.cucumber.eclipse.editor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.PlatformUI;

import io.cucumber.eclipse.editor.Images;
import io.cucumber.eclipse.editor.marker.MarkerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Provides quick fixes for language support markers.
 * When a project has a supported language nature but the support bundle is not installed,
 * this generator provides a resolution to install the support.
 */
public class LanguageSupportMarkerResolutionGenerator implements IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		try {
			if (hasResolutions(marker)) {
				String language = marker.getAttribute(MarkerFactory.LANGUAGE_SUPPORT_LANGUAGE_ATTRIBUTE, "");
				String bundleId = marker.getAttribute(MarkerFactory.LANGUAGE_SUPPORT_BUNDLE_ID_ATTRIBUTE, "");
				
				if (!language.isEmpty() && !bundleId.isEmpty()) {
					return new IMarkerResolution[] { 
						new InstallLanguageSupportResolution(language, bundleId) 
					};
				}
			}
		} catch (CoreException e) {
			// Log error but don't fail
		}
		return new IMarkerResolution[0];
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		try {
			return MarkerFactory.LANGUAGE_SUPPORT_AVAILABLE.equals(marker.getType());
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Resolution that installs language support bundle.
	 */
	private static class InstallLanguageSupportResolution implements IMarkerResolution {
		private final String language;
		private final String bundleId;

		public InstallLanguageSupportResolution(String language, String bundleId) {
			this.language = language;
			this.bundleId = bundleId;
		}

		@Override
		public String getLabel() {
			return String.format("Install %s support for Cucumber", language);
		}

		@Override
		public void run(IMarker marker) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			try {
				dialog.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(String.format("Installing %s support...", language), 100);
						
						// Simulate installation work - actual implementation will be added later
						for (int i = 0; i < 10; i++) {
							if (monitor.isCanceled()) {
								break;
							}
							Thread.sleep(1000);
							monitor.worked(10);
						}
						
						monitor.done();
					}
				});
				
				// After successful installation, delete the marker
				marker.delete();
			} catch (InvocationTargetException | InterruptedException | CoreException e) {
				// Handle error - actual implementation will show proper error dialog
			}
		}

		public Image getImage() {
			return Images.getCukesIcon();
		}
	}
}
