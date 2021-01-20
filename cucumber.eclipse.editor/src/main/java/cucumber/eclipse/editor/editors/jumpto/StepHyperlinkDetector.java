package cucumber.eclipse.editor.editors.jumpto;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import cucumber.eclipse.editor.editors.Editor;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinitionsRepository;
import io.cucumber.eclipse.editor.steps.StepDefinitionsStorage;

public class StepHyperlinkDetector implements IHyperlinkDetector {

	private Editor editor;
	
	public StepHyperlinkDetector(Editor editor) {
		this.editor = editor;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();
		if (document == null) {
			return null;
		}
		
		IFile gherkinFile = editor.getFile();
		
		int offset = region.getOffset();
		try {
			int selectionLineNumber = document.getLineOfOffset(offset) + 1;
			IMarker stepDefinitionMatchMarker = JumpToStepDefinition.findStepDefinitionMatchMarker(selectionLineNumber, gherkinFile);
			
			if(stepDefinitionMatchMarker != null) {
				IRegion lineInfo = document.getLineInformationOfOffset(offset);
				int lineStartOffset = lineInfo.getOffset();
				String currentLine = document.get(lineStartOffset, lineInfo.getLength());
				
				String stepDefinitionPath = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_PATH_ATTRIBUTE);
				String stepDefinitionJDTHandleIdentifier = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_JDT_HANDLE_IDENTIFIER_ATTRIBUTE);
				String stepDefinitionText = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE);
				Integer stepDefinitionLineNumber = (Integer) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_LINE_NUMBER_ATTRIBUTE);
				
				String id = (String) stepDefinitionMatchMarker
						.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_JDT_HANDLE_IDENTIFIER_ATTRIBUTE);
				//Search step in repository
				if (id != null) {
					StepDefinitionsRepository repository = StepDefinitionsStorage.INSTANCE
							.getOrCreate(gherkinFile.getProject(), null);
					Set<StepDefinition> stepDefinitions = repository.getAllStepDefinitions();
					for (StepDefinition stepDefinition : stepDefinitions) {
						if (id.equals(stepDefinition.getId())) {
							// define the hyperlink region
							String textStatement = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE);
							int statementStartOffset = lineStartOffset + currentLine.indexOf(textStatement);
				
							IRegion stepRegion = new Region(statementStartOffset, textStatement.length());
							
							return new IHyperlink[] { new StepHyperlink(stepRegion, stepDefinition) };
						}
					}
				}
				
				
				
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
