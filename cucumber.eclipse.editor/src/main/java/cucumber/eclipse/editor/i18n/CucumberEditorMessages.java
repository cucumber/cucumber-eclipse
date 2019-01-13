package cucumber.eclipse.editor.i18n;

import org.eclipse.osgi.util.NLS;

public class CucumberEditorMessages extends NLS {

	private static final String BUNDLE_NAME = "cucumber.eclipse.editor.i18n.CucumberEditorMessages"; //$NON-NLS-1$

    public static String MarkerResolution__Configure_as_cucumber_project;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CucumberEditorMessages.class);
	}
	
}



