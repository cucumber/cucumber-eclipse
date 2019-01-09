package cucumber.eclipse.editor.builder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
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

import cucumber.eclipse.editor.steps.BuildStorage;
import cucumber.eclipse.editor.steps.GlueRepository;
import cucumber.eclipse.editor.steps.GlueStorage;
import cucumber.eclipse.editor.steps.UniversalStepDefinitionsProvider;
import cucumber.eclipse.editor.util.FileUtil;
import cucumber.eclipse.steps.integration.Activator;
import cucumber.eclipse.steps.integration.GherkinStepWrapper;
import cucumber.eclipse.steps.integration.Glue;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.StepPreferences;
import cucumber.eclipse.steps.integration.filter.FilterUtil;
import cucumber.eclipse.steps.integration.filter.SameLocationFilter;
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
 * compilation of step definitions itself. For this work see
 * {#CucumberStepDefinitionsBuilder}
 * 
 * To maintain the glue, this builder compiles step defintions file to notify
 * gherkin files to check if they are impacted by the step definitions update.
 * 
 * This builder MUST BE USED AFTER the {#CucumberStepDefinitionsBuilder}, and
 * assume the StepDefinitionsRepository to be populated.
 * 
 * @author qvdk
 *
 */
public class CucumberGherkinBuilder extends IncrementalProjectBuilder {

	public static final String ID = "cucumber.eclipse.builder.gherkin";
	private MarkerFactory markerFactory = MarkerFactory.INSTANCE;
	private final UniversalStepDefinitionsProvider stepDefinitionsProvider = UniversalStepDefinitionsProvider.INSTANCE;
	private final BuildStorage<GlueRepository> glueStorage = GlueStorage.INSTANCE;

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		boolean glueDetectionEnabled = StepPreferences.INSTANCE.isStepDefinitionsMatchingEnabled();
		switch (kind) {
		case FULL_BUILD:
			System.out.println("gherkin full build");
			fullBuild(glueDetectionEnabled, monitor);
			break;
		case AUTO_BUILD:
		case INCREMENTAL_BUILD:
			IResourceDelta delta = getDelta(getProject());
			System.out.println("gherkin incrementale build on " + delta.getResource().getName());
			incrementalBuild(delta, glueDetectionEnabled, monitor);
			break;
		case CLEAN_BUILD:
			System.out.println("gherkin clean build");
			break;
		default:
			break;
		}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, boolean glueDetectionEnabled, IProgressMonitor monitor) throws CoreException {
		try {
			// the visitor does the work.
			delta.accept(new CucumberGherkinBuildVisitor(markerFactory, glueDetectionEnabled, monitor));
			glueStorage.persist(getProject(), monitor);
		} catch (CoreException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}

	}

	private void fullBuild(boolean glueDetectionEnabled, IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new CucumberGherkinBuildVisitor(markerFactory, glueDetectionEnabled, monitor));
			glueStorage.persist(getProject(), monitor);
		} catch (CoreException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	class CucumberGherkinBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {

		private IProgressMonitor monitor;
		private MarkerFactory markerFactory;
		private boolean glueDetectionEnabled;

		public CucumberGherkinBuildVisitor(MarkerFactory markerFactory, boolean glueDetectionEnabled,
				IProgressMonitor monitor) {
			this.monitor = monitor;
			this.markerFactory = markerFactory;
			this.glueDetectionEnabled = glueDetectionEnabled;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			return buildGherkin(resource, false, glueDetectionEnabled);
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			return buildGherkin(resource, true, glueDetectionEnabled);
		}

		protected boolean buildGherkin(IResource resource, boolean isIncrementalBuild, boolean glueDetectionEnabled)
				throws CoreException {

			// stop the visitor pattern if a cancellation was requested
			if (monitor.isCanceled()) {
				return false;
			}

			if(!resource.exists()) {
				// skip 
				return true;
			}
			
			long start = System.currentTimeMillis();

			if (!(resource instanceof IFile)) {
				return true;
			}

			// start of a very bad hack...
			// Resources from test-classes generate a scan of non necessary files
			// but I don't know how to identity them. For the moment filter with the path.
			// Commonly, features are stored in `src/test/resources` for java projects
			// and this files are copied by the Java compiler into
			// `${project.build.directory}/test-classes`
			// but we do not need this files since we already parse sources.
			if (resource.getFullPath().toString().contains("test-classes")) {
				return true;
			}
			// end of the very bad hack...

			IFile file = (IFile) resource;

			// Compile only gherkin files
			String fileExtension = file.getFileExtension();
			boolean isGherkinFile = "feature".equals(fileExtension) || "story".equals(fileExtension);
			if (!isGherkinFile) {
				if (!glueDetectionEnabled) {
					// in this case there are nothing to do
					return true;
				}
				// If this is not a gherkin file AND it is a step definitions file
				// then we shall rebuild all gherkins files because this step definitions file
				// could add glue to any of gherkins steps.

				if (isIncrementalBuild && stepDefinitionsProvider.support(getProject())) {
					// force a full build of gherkin files
					fullBuild(glueDetectionEnabled, monitor);
				}

				return true;
			}

//			System.out.println(String.format("gherkin %s builder compile: %s",
//					(isIncrementalBuild ? "incremental" : "full"), resource));
			GlueRepository glueRepository = glueStorage.getOrCreate(resource.getProject());
			glueRepository.clean(resource);
			this.markerFactory.cleanMarkers(resource);

			try {
				String gherkinSource = FileUtil.read(file);
				MarkerFormatter formatter = new MarkerFormatter(new Document(gherkinSource), resource, markerFactory,
						glueDetectionEnabled);
				Parser gherkinParser = new Parser(formatter, false);
				gherkinParser.parse(gherkinSource, file.getFullPath().toString(), 0);
			} catch (LexingError e) {
				String firstLine = e.getMessage().split("\\R", 2)[0];
				Pattern pattern = Pattern.compile("Lexing error on line (\\d+): '(.*)");
				Matcher matcher = pattern.matcher(firstLine);
				String error;
				int lineNumber = 0;
				if (matcher.matches()) {
					lineNumber = Integer.valueOf(matcher.group(1));
					error = matcher.group(2);
				} else {
					lineNumber = 0;
					error = e.getMessage();
				}
				RuntimeException runtimeException = new RuntimeException("Syntax error: " + error);

				markerFactory.syntaxErrorOnGherkin(resource, runtimeException, lineNumber);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}

			long end = System.currentTimeMillis();
			System.out.println("Gherkin Compile " + resource.getName() + " in " + (end - start) + " ms.");
			return true;
		}

	}

	/**
	 * The formatter is misnamed. It is a implementation of the visitor pattern.
	 * Thus, format is not the only possibility for this class. In our case, we will
	 * use it to build the glue between gherkin steps and thiers definitions. More,
	 * we will place markers.
	 */
	protected class MarkerFormatter implements Formatter {

		private IResource gherkinFile;
		private IDocument gherkinDocument;
		private IProject project;
		private MarkerFactory markerFactory;
		private boolean inScenarioOutline = false;
		private List<gherkin.formatter.model.Step> scenarioOutlineSteps;
		private boolean isGlueDetectionEnabled;
		private GlueRepository glueRepository;
		private StepPreferences stepPreferences = StepPreferences.INSTANCE;

		public MarkerFormatter(IDocument document, IResource gherkinFile, MarkerFactory markerFactory,
				boolean isGlueDetectionEnabled) throws CoreException {
			this.gherkinFile = gherkinFile;
			this.project = gherkinFile.getProject();
			this.gherkinDocument = document;
			this.markerFactory = markerFactory;
			this.isGlueDetectionEnabled = isGlueDetectionEnabled;
			this.glueRepository = glueStorage.getOrCreate(this.project);
		}

		@Override
		public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
			this.markerFactory.syntaxErrorOnGherkin(this.gherkinFile, new ParseException(event, line));
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
				try {
					matchScenarioOutlineExample(examplesHeader, currentExample);
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}
		}

		private void matchScenarioOutlineExample(ExamplesTableRow header, ExamplesTableRow example) throws CoreException {
			Map<String, String> exampleVariablesMap = getExampleVariablesMap(header, example);
			for (gherkin.formatter.model.Step scenarioOutlineStepLine : scenarioOutlineSteps) {
				validate(scenarioOutlineStepLine, exampleVariablesMap, example.getLine());
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
		 * @throws CoreException if any exception occurs 
		 */
		protected void validate(Step step) throws CoreException {
			if (!isGlueDetectionEnabled) {
				return;
			}
			Set<StepDefinition> allStepDefinitions = stepDefinitionsProvider
					.getStepDefinitions(this.project);
			Set<StepDefinition> stepDefinitionsScope = this.filter((IFile) gherkinFile, allStepDefinitions);
			StepDefinition glueStepDefinition = null;

			for (StepDefinition stepDefinition : stepDefinitionsScope) {
				boolean matches = stepDefinition.matches(step.getName());
				if (matches) {
					glueStepDefinition = stepDefinition;
					break;
				}
			}
			boolean isFound = glueStepDefinition != null;
			GherkinStepWrapper gherkinStepWrapper = new GherkinStepWrapper(step, gherkinFile);
			if (isFound) {
				Glue glue = glueRepository.add(gherkinStepWrapper, glueStepDefinition);
				markerFactory.glueFound(glue);
			} else {
				markerFactory.unmatchedStep(gherkinDocument, gherkinStepWrapper);
				// if the step was defined before, we need to remove its glue
				glueRepository.clean(step);
			}
		}

		/**
		 * There are a particular way to process scenario outline step, since the
		 * validation of examples should match with the previously parsed step.
		 * 
		 * @param scenarioOutlineStepLine the scenario outline
		 * @param exampleVariablesMap     the examples
		 * @param exampleLine             the line
		 * @throws CoreException if any exception occurs 
		 */
		private void validate(Step scenarioOutlineStepLine, Map<String, String> exampleVariablesMap,
				Integer exampleLine) throws CoreException {
			if (!isGlueDetectionEnabled) {
				return;
			}
			String derivateGherkinStepSource = getResolvedStepStringForExample(scenarioOutlineStepLine,
					exampleVariablesMap);

			Set<cucumber.eclipse.steps.integration.StepDefinition> allStepDefinitions = stepDefinitionsProvider
					.getStepDefinitions(project);
			Set<StepDefinition> stepDefinitionsScope = this.filter((IFile) gherkinFile, allStepDefinitions);
			cucumber.eclipse.steps.integration.StepDefinition glueStepDefinition = null;

			for (StepDefinition stepDefinition : stepDefinitionsScope) {
				boolean matches = stepDefinition.matches(derivateGherkinStepSource);
				if (matches) {
					glueStepDefinition = stepDefinition;
					break;
				}
			}
			boolean isFound = glueStepDefinition != null;
			GherkinStepWrapper gherkinStepWrapper = new GherkinStepWrapper(scenarioOutlineStepLine, gherkinFile);
			if (isFound) {
				Glue glue = glueRepository.add(gherkinStepWrapper, glueStepDefinition);
				markerFactory.glueFound(glue);
			} else {
				markerFactory.unmatchedStep(gherkinDocument, gherkinStepWrapper);
				markerFactory.gherkinStepExampleUnmatch(gherkinDocument, gherkinFile, exampleLine);
			}

		}
		
		private Set<StepDefinition> filter(IFile gherkinFile, Set<StepDefinition> stepDefinitions) {
			
			Set<StepDefinition> filtered = new HashSet<StepDefinition>();
			String gherkinLocation = gherkinFile.getParent().getFullPath().toString();
			
			boolean shouldFilterInSameLocation = this.stepPreferences.isGlueOnlyInSameLocationEnabled();
			if(!shouldFilterInSameLocation) {
				return stepDefinitions;
			}
			
			SameLocationFilter sameLocationFilter = new SameLocationFilter(gherkinLocation);
			FilterUtil.filter(stepDefinitions, sameLocationFilter, filtered);
			
			return filtered;
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

			try {
				validate(step);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
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
			// stop the visitor pattern if a cancellation was requested
			if (monitor.isCanceled()) {
				return false;
			}

			this.markerFactory.cleanMarkers(resource);
			GlueRepository glueRepository = glueStorage.getOrCreate(getProject());
			glueRepository.clean(resource);

			return true;
		}

	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		System.out.println("CucumberGherkinBuilder.clean");

		getProject().accept(new CucumberGherkinCleanBuildVisitor(markerFactory, monitor));
	}
	
	
}
