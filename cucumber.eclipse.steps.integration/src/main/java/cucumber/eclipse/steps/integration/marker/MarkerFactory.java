package cucumber.eclipse.steps.integration.marker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;

import cucumber.eclipse.steps.integration.Activator;
import cucumber.eclipse.steps.integration.GherkinStepWrapper;
import cucumber.eclipse.steps.integration.Glue;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.i18n.CucumberStepsIntegrationMessages;
import gherkin.formatter.model.Step;

/**
 * The marker factory exposes methods to put makers:
 *  <li>unmatched step</li>
 *  <li>gherkin syntax error</li>
 *  <li>glue between step definition and gherkin step</li>
 *  <li>feature file opened from a non cucumber project</li>
 * 
 * @author qvdk
 *
 */
public class MarkerFactory {

	public static final MarkerFactory INSTANCE = new MarkerFactory();

	public static final String CUCUMBER_MARKER = "cucumber.eclipse.marker";
	public static final String STEPDEF_SYNTAX_ERROR = CUCUMBER_MARKER + ".stepdef.syntaxerror";
	public static final String GHERKIN_SYNTAX_ERROR = CUCUMBER_MARKER + ".gherkin.syntaxerror";

	public static final String STEP_DEFINTION_MATCH = CUCUMBER_MARKER + ".stepdef.matches";
	public static final String STEP_DEFINITION_MATCH_PATH_ATTRIBUTE = STEP_DEFINTION_MATCH + ".path";
	public static final String STEP_DEFINITION_MATCH_JDT_HANDLE_IDENTIFIER_ATTRIBUTE = STEP_DEFINTION_MATCH + ".jdt_handler_identifier";
	public static final String STEP_DEFINITION_MATCH_LINE_NUMBER_ATTRIBUTE = STEP_DEFINTION_MATCH + ".line_number";
	public static final String STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE = STEP_DEFINTION_MATCH + ".text";

	public static final String SCENARIO_OUTLINE_EXAMPLE_UNMATCH = CUCUMBER_MARKER + ".scenario_outline_example_unmatch";
	public static final String MULTIPLE_STEP_DEFINTIONS_MATCH = CUCUMBER_MARKER + ".stepdef.multiple_matches";

	public static final String UNMATCHED_STEP = CUCUMBER_MARKER + ".gherkin.unmatched_step";
	public static final String UNMATCHED_STEP_KEYWORD_ATTRIBUTE = UNMATCHED_STEP + ".keyword";
	public static final String UNMATCHED_STEP_NAME_ATTRIBUTE = UNMATCHED_STEP + ".name";
	public static final String UNMATCHED_STEP_PATH_ATTRIBUTE = UNMATCHED_STEP + ".path";

	public static final String NOT_A_CUCUMBER_PROJECT = CUCUMBER_MARKER + ".not_a_cucumber_project";
	public static final String NOT_A_CUCUMBER_PROJECT_NAME_ATTRIBUTE = NOT_A_CUCUMBER_PROJECT + ".project_name";
	
	public static final String CUCUMBER_NATURE_MISSING_MARKER = CUCUMBER_MARKER + ".project.cucumber_nature_missing";

	private MarkerFactory() {
	}

	public void syntaxErrorOnStepDefinition(IResource stepDefinitionResource, Exception e) {
		syntaxErrorOnStepDefinition(stepDefinitionResource, e, 0);
	}

//	public void multipleStepDefinitionsMatched(final IResource gherkinFile, final gherkin.formatter.model.Step gherkinStep, final StepDefinition... stepDefinitions) {
//		this.mark(gherkinFile, new IMarkerBuilder() {
//			@Override
//			public IMarker build() {
//				IMarker marker = null;
//				
//				
//				try {
//					marker = gherkinFile.createMarker(MULTIPLE_STEP_DEFINTIONS_MATCH);
//					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
//					marker.setAttribute(IMarker.LINE_NUMBER, gherkinStep.getLine());
//					
//					StringBuffer stepDefinitionsNamesStringBuffer = new StringBuffer();
//					StringBuffer stepDefinitionsFullPathsStringBuffer = new StringBuffer();
//
//					for (int it = 0 ; it < stepDefinitions.length; it++) {
//						StepDefinition stepDefinition = stepDefinitions[it];
//						stepDefinitionsNamesStringBuffer.append(stepDefinition.getSource().getName()).append(":")
//								.append(stepDefinition.getLineNumber());
//						
//						stepDefinitionsFullPathsStringBuffer.append(stepDefinition.getSource().getFullPath()).append(":")
//						.append(stepDefinition.getLineNumber());
//						
//						if(it == stepDefinitions.length) {
//							stepDefinitionsNamesStringBuffer.append(",");
//							stepDefinitionsFullPathsStringBuffer.append(",");
//						}
//					}
//					
//					String message = String.format(
//							"Step '%s' have more than one glue code: %s",
//							gherkinStep.getName(), stepDefinitionsNamesStringBuffer.toString());
//					
//					marker.setAttribute(IMarker.MESSAGE, message);
//					marker.setAttribute("duplicates", stepDefinitionsFullPathsStringBuffer.toString());
//					
//				} catch (CoreException e) {
//					e.printStackTrace();
//				}
//				return marker;
//			}
//		});
//	}

	public void unmatchedStep(final IDocument gherkinDocument, final GherkinStepWrapper gherkinStepWrapper) {

		final IResource gherkinFile = gherkinStepWrapper.getSource();
		final Step gherkinStep = gherkinStepWrapper.getStep();
		final int lineNumber = gherkinStep.getLine();

		this.mark(gherkinFile, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;

				String warningMessage = String.format("Step '%s' does not have a matching glue code%s",
						gherkinStep.getName(), "");

				int lineStartOffset = 0;

				IRegion lineInfo = null;
				String currentLine = null;
				try {
					lineInfo = gherkinDocument.getLineInformation(lineNumber - 1);
					lineStartOffset = lineInfo.getOffset();
					currentLine = gherkinDocument.get(lineStartOffset, lineInfo.getLength());
				} catch (BadLocationException e) {
					return null;
				}

				String textStatement = gherkinStep.getName();
				int statementStartOffset = lineStartOffset + currentLine.indexOf(textStatement);

				IRegion stepRegion = new Region(statementStartOffset, textStatement.length());
				try {
					marker = gherkinFile.createMarker(UNMATCHED_STEP);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.MESSAGE, warningMessage);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					marker.setAttribute(IMarker.CHAR_START, stepRegion.getOffset());
					marker.setAttribute(IMarker.CHAR_END, stepRegion.getOffset() + stepRegion.getLength());
					marker.setAttribute(UNMATCHED_STEP_KEYWORD_ATTRIBUTE, gherkinStep.getKeyword());
					marker.setAttribute(UNMATCHED_STEP_NAME_ATTRIBUTE, gherkinStep.getName());
					marker.setAttribute(UNMATCHED_STEP_PATH_ATTRIBUTE, gherkinFile.getFullPath().toString());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
	}

	public void syntaxErrorOnStepDefinition(final IResource stepDefinitionResource, final Exception e,
			final int lineNumber) {

		this.mark(stepDefinitionResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					marker = stepDefinitionResource.createMarker(STEPDEF_SYNTAX_ERROR);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, e.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});

	}

	public void syntaxErrorOnGherkin(IResource stepDefinitionResource, Exception e) {
		syntaxErrorOnGherkin(stepDefinitionResource, e, 0);
	}

	public void syntaxErrorOnGherkin(final IResource gherkinResource, final Exception e, final int lineNumber) {

		this.mark(gherkinResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					marker = gherkinResource.createMarker(GHERKIN_SYNTAX_ERROR);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, e.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});

	}

	public void glueFound(final Glue glue) {
		Step step = glue.getGherkinStepWrapper().getStep();
		this.glueFound(glue, step.getName(), step.getLine());
	}

	public void glueFound(final Glue glue, final String stepDefinitionText, final int lineNumber) {

		final IResource gherkinResource = glue.getGherkinStepWrapper().getSource();
		final StepDefinition stepDefinition = glue.getStepDefinition();

		this.mark(gherkinResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					marker = gherkinResource.createMarker(STEP_DEFINTION_MATCH);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
					String message = String.format("Glued with %s", stepDefinition.getLabel());
					marker.setAttribute(IMarker.MESSAGE, message);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					if(stepDefinition.getSource() != null) {
						marker.setAttribute(STEP_DEFINITION_MATCH_PATH_ATTRIBUTE,
								stepDefinition.getSource().getFullPath().toString());
					}
					marker.setAttribute(STEP_DEFINITION_MATCH_JDT_HANDLE_IDENTIFIER_ATTRIBUTE, stepDefinition.getJDTHandleIdentifier());
					marker.setAttribute(STEP_DEFINITION_MATCH_LINE_NUMBER_ATTRIBUTE, stepDefinition.getLineNumber());
					marker.setAttribute(STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE, stepDefinitionText);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});

	}

	public void gherkinStepExampleUnmatch(final IDocument gherkinDocument, final IResource gherkinResource,
			final int lineNumber) {

		this.mark(gherkinResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				int lineStartOffset = 0;

				IRegion lineInfo = null;
				String currentLine = null;
				try {
					lineInfo = gherkinDocument.getLineInformation(lineNumber - 1);
					lineStartOffset = lineInfo.getOffset();
					currentLine = gherkinDocument.get(lineStartOffset, lineInfo.getLength());
				} catch (BadLocationException e) {
					return null;
				}

				String currentLineTrim = currentLine.trim();
				int statementStartOffset = lineStartOffset + currentLine.indexOf(currentLineTrim);

				IRegion stepRegion = new Region(statementStartOffset, currentLineTrim.length());

				try {
					marker = gherkinResource.createMarker(SCENARIO_OUTLINE_EXAMPLE_UNMATCH);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.MESSAGE,
							String.format("No compatible step definition with %s", currentLineTrim));
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					marker.setAttribute(IMarker.CHAR_START, stepRegion.getOffset());
					marker.setAttribute(IMarker.CHAR_END, stepRegion.getOffset() + stepRegion.getLength());

				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});

	}
	
	public void featureFileIsNotInCucumberProject(IFile project) {
		this.mark(project, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					project.deleteMarkers(NOT_A_CUCUMBER_PROJECT, true, IResource.DEPTH_ZERO);
					marker = project.createMarker(NOT_A_CUCUMBER_PROJECT);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.MESSAGE, CucumberStepsIntegrationMessages.MarkerFactory__Step_definitions_detection_not_working_on_non_cucumber_project);
					marker.setAttribute(IMarker.LINE_NUMBER, 1);
					marker.setAttribute(NOT_A_CUCUMBER_PROJECT_NAME_ATTRIBUTE, project.getName());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
	}

	public void cleanMarkers(IResource resource) {
		try {
			resource.deleteMarkers(CUCUMBER_MARKER, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Couldn't remove markers from %s", resource), e));
		}
	}

	public void cleanMarkersRecursively(IResource resource) {
		try {
			resource.deleteMarkers(CUCUMBER_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Couldn't remove markers from %s", resource), e));
		}
	}
	
	private void mark(final IResource resource, final IMarkerBuilder markerBuilder) {
		try {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					markerBuilder.build();
				}
			};

			resource.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);

		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Failed to place marker %s", resource), e));
		}
	}

	private interface IMarkerBuilder {
		IMarker build();
	}

	public void cleanCucumberNatureMissing(IProject project) throws CoreException {
		IMarker[] markers = project.findMarkers(CUCUMBER_NATURE_MISSING_MARKER, false,
				IResource.DEPTH_ZERO);
		for (IMarker marker : markers) {
			marker.delete();
		}
	}

}
