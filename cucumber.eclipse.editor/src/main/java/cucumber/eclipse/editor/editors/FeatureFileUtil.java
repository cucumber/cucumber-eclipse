package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.Step;

class FeatureFileUtil{
	final static String EXTENSION_POINT_STEPDEFINITIONS_ID = "cucumber.eclipse.steps.integration";

	static String getDocumentLanguage(IEditorPart editorPart) {
		String lang = null;
		try {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocumentProvider docProvider = editor.getDocumentProvider();
			IDocument doc = docProvider.getDocument(editorPart
					.getEditorInput());

			IRegion lineInfo = doc.getLineInformation(0);
			int length = lineInfo.getLength();
			int offset = lineInfo.getOffset();
			String line = doc.get(offset, length);

			if (line.contains("language")) {
				int indexOf = line.indexOf(":");
				lang = line.substring((indexOf + 1)).trim();
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return lang;
	}
	
	static Set<Step> getStepsInEncompassingProject(IFile featurefile) {
		Set<Step> steps = new HashSet<Step>();
		for (IStepDefinitions stepDef : FeatureFileUtil.getStepDefinitions()) {
			steps.addAll(stepDef.getSteps(featurefile));
		}
		return steps;
	}

	private static List<IStepDefinitions> getStepDefinitions() {
		List<IStepDefinitions> stepDefs = new ArrayList<IStepDefinitions>();
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(FeatureFileUtil.EXTENSION_POINT_STEPDEFINITIONS_ID);
		try {
			for (IConfigurationElement ce : config) {
				Object obj = ce.createExecutableExtension("class");
				if (obj instanceof IStepDefinitions) {
					stepDefs.add((IStepDefinitions) obj);
				}
			}
		} catch (CoreException e) {
		}
		return stepDefs;
	}
}