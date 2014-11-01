package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.Step;

public class PopupMenuFindStepActionDelegate extends AbstractHandler {

	private final static String EXTENSION_POINT_STEPDEFINITIONS_ID = "cucumber.eclipse.steps.integration";
	private Pattern cukePattern = Pattern.compile("(?:Given|When|Then|And|But) (.*)$");
//	private Pattern cukePattern = Pattern.compile("(?:Givet|När|Så|Och|Men) (.*)$");
	private Pattern variablePattern = Pattern.compile("<([^>]+)>");

	private List<IStepDefinitions> getStepDefinitions() {
		List<IStepDefinitions> stepDefs = new ArrayList<IStepDefinitions>();
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_STEPDEFINITIONS_ID);
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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);
		IEditorInput input = editorPart.getEditorInput();

		// Editor contents needs to be associated with an eclipse project
		// for this to work, if not then simply do nothing.
		if (!(input instanceof IFileEditorInput)) {
			return null;
		}
		IFile featurefile = ((IFileEditorInput) input).getFile();

		Set<Step> steps = new HashSet<Step>();
		for (IStepDefinitions stepDef : getStepDefinitions()) {
			steps.addAll(stepDef.getSteps(featurefile));
		}

		String selectedLine = getSelectedLine(editorPart);
		String language = getDocumentLanguage(editorPart);
		
		if(language != null) {
			System.out.println(language);
			if(language.toLowerCase().equals("sv")) {
				this.cukePattern = Pattern.compile("(?:Givet|När|Så|Och|Men) (.*)$");
			}
		}
		
		Step matchedStep = matchSteps(language, steps, selectedLine);
		try {
			if (matchedStep != null) openEditor(matchedStep);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
		


	Step matchSteps(String language, Set<Step> steps, String currentLine) {
		Matcher matcher = cukePattern.matcher(currentLine);
		if (matcher.matches()) {
			String cukeStep = matcher.group(1);
		
			// FIXME: Replace variables with 0 for now to allow them to match steps
			// Should really read the whole scenario outline and sub in the first scenario
			Matcher variableMatcher = variablePattern.matcher(cukeStep);
			cukeStep = variableMatcher.replaceAll("0");
			
			for(Step step: steps) {
				if (step.matches(cukeStep)) {
					return step;
				}
			}
			
		} 
		return null;
	}
	
	private void openEditor(Step step) throws CoreException {
		   IResource file = step.getSource();
		   IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		   HashMap<String, Integer> map = new HashMap<String, Integer>();
		   map.put(IMarker.LINE_NUMBER, step.getLineNumber());
		   IMarker marker = file.createMarker(IMarker.TEXT);
		   marker.setAttributes(map);
		   IDE.openEditor(page, marker);
		   marker.delete();
		   
	}
	
	private String getDocumentLanguage(IEditorPart editorPart) {
		String lang = null;
		try {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocumentProvider docProvider = editor.getDocumentProvider();
			IDocument doc = docProvider.getDocument(editorPart.getEditorInput());
			
			IRegion lineInfo = doc.getLineInformation(0);
			int length = lineInfo.getLength();
			int offset = lineInfo.getOffset();
			String line = doc.get(offset, length);
			
			if(line.contains("language")) {
				int indexOf = line.indexOf(":");
				lang = line.substring((indexOf + 1)).trim();
			}
		} catch(BadLocationException e) {
			e.printStackTrace();
		}
		
		return lang;
	}
	
	private String getSelectedLine(IEditorPart editorPart) {
		
		ITextEditor editor = (ITextEditor) editorPart;
				
		TextSelection selecton = (TextSelection) editor.getSelectionProvider().getSelection();
		int line = selecton.getStartLine();	
		
		IDocumentProvider docProvider = editor.getDocumentProvider();
		IDocument doc = docProvider.getDocument(editorPart.getEditorInput());
		try {
			String stepLine = doc.get(doc.getLineOffset(line), doc.getLineLength(line)).trim();
			return stepLine;
		} catch (BadLocationException e) {
			return "";
		}
	}
}
