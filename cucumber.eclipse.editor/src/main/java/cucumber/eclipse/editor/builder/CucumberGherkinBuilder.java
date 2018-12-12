package cucumber.eclipse.editor.builder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import cucumber.eclipse.editor.steps.ExtensionRegistryStepProvider;
import cucumber.eclipse.editor.steps.GherkinStepWrapper;
import cucumber.eclipse.editor.steps.GlueRepository;
import cucumber.eclipse.editor.steps.GlueRepository.Glue;
import cucumber.eclipse.editor.util.FileUtil;
import cucumber.eclipse.steps.integration.Activator;
import cucumber.eclipse.steps.integration.StepDefinitionsRepository;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.lexer.LexingError;
import gherkin.parser.Parser;

/**
 * Incremental builder of gherkin steps.
 * 
 * This builder has many roles :
 * 
 * <ol>
 * <li>parse gherkin files</li>
 * <li>maintain a repository of gherkin steps</li>
 * <li>maintain the glue between gherkin steps and step definitions</li>
 * </ol>
 * 
 * Thus, this builder compiles gherkin file to extract steps and compiles step
 * definitions to maintain glue. This builder HAVE NOT the responsibility of the
 * compilation of step definitions itself. For this work see {#CucumberBuilder}
 * 
 * To maintain the glue, this builder compiles step defintions file to notify 
 * gherkin files to check if they are impacted by the step definitions update.
 * 
 * This builder MUST USED AFTER the {#CucumberBuilder}, and assume the
 * StepDefinitionsRepository to be populated.
 * 
 * @author qvdk
 *
 */
public class CucumberGherkinBuilder extends IncrementalProjectBuilder {

	public static final String ID = "cucumber.eclipse.builder.gherkin";
	private MarkerFactory markerFactory = new MarkerFactory();
	private final ExtensionRegistryStepProvider stepDefinitionsProvider = ExtensionRegistryStepProvider.INSTANCE;
	private StepDefinitionsRepository stepDefinitionsRepository = StepDefinitionsRepository.INSTANCE;
	private final GlueRepository glueRepository = GlueRepository.INSTANCE;

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		switch (kind) {
		case FULL_BUILD:
			System.out.println("gherkin full build");
			fullBuild(monitor);
			break;
		case AUTO_BUILD:
		case INCREMENTAL_BUILD:
			IResourceDelta delta = getDelta(getProject());
			System.out.println("gherkin incrementale build on " + delta.getResource().getName());
			incrementalBuild(delta, monitor);
			break;
		case CLEAN_BUILD:
			System.out.println("clean build");
			break;
		default:
			break;
		}

		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
		try {
			// the visitor does the work.
			delta.accept(new CucumberGherkinBuildVisitor(markerFactory, monitor));
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private void fullBuild(IProgressMonitor monitor) {
		try {
			getProject().accept(new CucumberGherkinBuildVisitor(markerFactory, monitor));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	class CucumberGherkinBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {

		private IProgressMonitor monitor;
		private MarkerFactory markerFactory;

		public CucumberGherkinBuildVisitor(MarkerFactory markerFactory, IProgressMonitor monitor) {
			this.monitor = monitor;
			this.markerFactory = markerFactory;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			return buildGherkin(resource, false);
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			System.out.println("gherkin builder compile: "+resource);
			return buildGherkin(resource, true);
		}

		protected boolean buildGherkin(IResource resource, boolean isIncrementalBuild) throws CoreException {
			long start = System.currentTimeMillis();
			//System.out.println("gherkin build " + resource.getName());
			
			if (!(resource instanceof IFile)) {
				return true;
			}

			// start of a very bad hack...
			// Resources from test-classes generate a scan of non necessary files.
			// but I don't know how to identity them. For the moment filter with the path.
			if(resource.getFullPath().toString().contains("test-classes")) {
				return true;
			}
			// end of the very bad hack...
			
			IFile file = (IFile) resource;

			// Compile only gherkin files
			String fileExtension = file.getFileExtension();
			boolean isGherkinFile = "feature".equals(fileExtension) || "story".equals(fileExtension);
			if (!isGherkinFile) {
				// If this is not a gherkin file AND it is a step definitions file
				// then we shall rebuild all gherkins file because this step definitions file
				// could add glue to any of gherkins steps.
				if(isIncrementalBuild && stepDefinitionsRepository.isStepDefinitions(file)) {
					// force a full build of gherkin
					file.getProject().build(FULL_BUILD, monitor);
				}
				
				return true;
			}

			this.markerFactory.cleanMarkers(resource);

			try {
				String gherkinSource = FileUtil.read(file);
				MarkerFormatter formatter = new MarkerFormatter(new Document(gherkinSource), resource, markerFactory);
				Parser gherkinParser = new Parser(formatter, false);
				gherkinParser.parse(gherkinSource, file.getFullPath().toString(), 0);
			} catch (LexingError e) {
				String firstLine = e.getMessage().split("\\R", 2)[0];
				Pattern pattern = Pattern.compile("Lexing error on line (\\d+): '(.*)");
				Matcher matcher = pattern.matcher(firstLine);
				String error;
				int lineNumber = 0; 
				if(matcher.matches()) {
					lineNumber = Integer.valueOf(matcher.group(1));
					error = matcher.group(2);
				}
				else {
					lineNumber = 0;
					error = e.getMessage();
				}
				RuntimeException runtimeException = new RuntimeException("Syntax error on " + error);

				markerFactory.syntaxErrorOnStepDefinition(resource, runtimeException, lineNumber);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}

			long end = System.currentTimeMillis();
			System.out.println("Gherkin Compile " + resource.getName() + " in " + (end - start) + " ms.");
			return true;
		}

	}

	protected class MarkerFormatter implements Formatter {

		private IResource gherkinFile;
		private IDocument gherkinDocument;
		private MarkerFactory markerFactory;
		private boolean inScenarioOutline = false;
		private String scenarioOutlineTemplate = "";
		private List<gherkin.formatter.model.Step> scenarioOutlineSteps;

		public MarkerFormatter(IDocument document, IResource gherkinFile, MarkerFactory markerFactory) {
			this.gherkinFile = gherkinFile;
			this.gherkinDocument = document;
			this.markerFactory = markerFactory;
		}

		@Override
		public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
			this.markerFactory.syntaxErrorOnStepDefinition(this.gherkinFile, new ParseException(event, line));
		}

		@Override
		public void uri(String uri) {
		}

		@Override
		public void feature(Feature feature) {
		}

		@Override
		public void scenarioOutline(ScenarioOutline scenarioOutline) {
			inScenarioOutline = true;
			scenarioOutlineSteps = new ArrayList<gherkin.formatter.model.Step>();
		}

		@Override
		public void examples(Examples examples) {
			ExamplesTableRow examplesHeader = examples.getRows().get(0);
			for (int i = 1; i < examples.getRows().size(); i++) {
				ExamplesTableRow currentExample = examples.getRows().get(i);
				matchScenarioOutlineExample(examplesHeader, currentExample);
			}
		}

		private void matchScenarioOutlineExample(ExamplesTableRow header, ExamplesTableRow example) {
			Map<String, String> exampleVariablesMap = getExampleVariablesMap(header, example);
			for (gherkin.formatter.model.Step scenarioOutlineStepLine : scenarioOutlineSteps) {
				validate(scenarioOutlineStepLine, exampleVariablesMap, example.getLine());
			}
		}

		/**
		 * @param scenarioOutlineStepLine the scenario outline
		 * @param exampleVariablesMap     the examples
		 * @param exampleLine             the line
		 */
		private void validate(Step scenarioOutlineStepLine, Map<String, String> exampleVariablesMap,
				Integer exampleLine) {
			
			String derivateGherkinStepSource = getResolvedStepStringForExample(scenarioOutlineStepLine,
					exampleVariablesMap);
			
			Set<cucumber.eclipse.steps.integration.Step> allStepDefinitions = stepDefinitionsProvider
					.getStepsInEncompassingProject();
			cucumber.eclipse.steps.integration.Step glueStepDefinition = null;

			for (cucumber.eclipse.steps.integration.Step stepDefinition : allStepDefinitions) {
				boolean matches = stepDefinition.matches(derivateGherkinStepSource);
				if (matches) {
					glueStepDefinition = stepDefinition;
					break;
				}
			}
			boolean isFound = glueStepDefinition != null;
			if (isFound) {
				markerFactory.gherkinStepWithDefinitionFound(gherkinFile, glueStepDefinition, scenarioOutlineStepLine.getLine());
				glueRepository.add(new GherkinStepWrapper(scenarioOutlineStepLine, gherkinFile), glueStepDefinition);
			} else {
				markerFactory.gherkinStepExampleUnmatch(gherkinDocument, gherkinFile, exampleLine);
//				markerFactory.unmatchedStep(gherkinDocument, gherkinFile, scenarioOutlineStepLine, exampleLine);
			}
			
		}

		private String getResolvedStepStringForExample(Step stepLine, Map<String, String> examplesLineMap) {
			String derivateGherkinStep = stepLine.getName();
			if (examplesLineMap != null) {
				for (Map.Entry<String, String> examplesLineEntry : examplesLineMap.entrySet()) {
					derivateGherkinStep = derivateGherkinStep.replace("<" + examplesLineEntry.getKey() + ">",
							examplesLineEntry.getValue());
				}
			}

			return derivateGherkinStep;
		}

		
		/**
		 * Check if the step have a matching step definitions.
		 * 
		 * @param step a gherkin step
		 */
		protected void validate(Step step) {
			Set<cucumber.eclipse.steps.integration.Step> allStepDefinitions = stepDefinitionsProvider
					.getStepsInEncompassingProject();
			cucumber.eclipse.steps.integration.Step glueStepDefinition = null;

			for (cucumber.eclipse.steps.integration.Step stepDefinition : allStepDefinitions) {
				boolean matches = stepDefinition.matches(step.getName());
				if (matches) {
					glueStepDefinition = stepDefinition;
					break;
				}
			}
			boolean isFound = glueStepDefinition != null;
			if (isFound) {
				markerFactory.gherkinStepWithDefinitionFound(gherkinFile, glueStepDefinition, step.getLine());
				glueRepository.add(new GherkinStepWrapper(step, gherkinFile), glueStepDefinition);
			} else {
				markerFactory.unmatchedStep(gherkinDocument, gherkinFile, step, step.getLine());
			}
		}

		private Map<String, String> getExampleVariablesMap(ExamplesTableRow header, ExamplesTableRow values) {
			Map<String, String> result = new LinkedHashMap<String, String>();
			for (int i = 0; i < header.getCells().size(); i++) {
				result.put(header.getCells().get(i), values.getCells().get(i));
			}
			return result;
		}

		@Override
		public void startOfScenarioLifeCycle(Scenario scenario) {
		}

		@Override
		public void background(Background background) {
		}

		@Override
		public void scenario(Scenario scenario) {
			inScenarioOutline = false;
		}

		@Override
		public void step(Step step) {
			if (inScenarioOutline) {
				scenarioOutlineSteps.add(step);
				return;
			}

			validate(step);
		}

		@Override
		public void endOfScenarioLifeCycle(Scenario scenario) {

		}

		@Override
		public void done() {

		}

		@Override
		public void close() {

		}

		@Override
		public void eof() {
		}

	}

	
	class CucumberGherkinCleanBuildVisitor implements IResourceVisitor {

		private IProgressMonitor monitor;
		private MarkerFactory markerFactory;

		public CucumberGherkinCleanBuildVisitor(MarkerFactory markerFactory, IProgressMonitor monitor) {
			this.monitor = monitor;
			this.markerFactory = markerFactory;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			this.markerFactory.cleanMarkers(resource);
			return true;
		}

	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		System.out.println("CucumberGherkinBuilder.clean");
		GlueRepository.INSTANCE.clean();
		
		getProject().accept(new CucumberGherkinBuildVisitor(markerFactory, monitor));
	}
}
