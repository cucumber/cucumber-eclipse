package io.cucumber.eclipse.editor.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;

/**
 * Structure creator for Cucumber/Gherkin feature files.
 * <p>
 * This class provides structured compare support for .feature files in the Eclipse Compare framework.
 * It parses Gherkin documents and creates a hierarchical structure of nodes representing Features,
 * Scenarios, Steps, etc., enabling semantic comparison rather than just line-by-line text comparison.
 * </p>
 * 
 * @see IStructureCreator
 */
public class GherkinStructureCreator implements IStructureCreator {

	@Override
	public String getName() {
		return "Gherkin Structure Compare";
	}

	@Override
	public IStructureComparator getStructure(Object input) {
		if (!(input instanceof IStreamContentAccessor)) {
			// Log when input type is unexpected to aid debugging
			System.err.println("GherkinStructureCreator: Unexpected input type: " + 
				(input != null ? input.getClass().getName() : "null"));
			return null;
		}

		IStreamContentAccessor accessor = (IStreamContentAccessor) input;
		try (InputStream stream = accessor.getContents()) {
			String content = readContent(stream, accessor);
			return createStructure(content, input);
		} catch (CoreException e) {
			System.err.println("GherkinStructureCreator: Error reading content: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.err.println("GherkinStructureCreator: IO error reading content: " + e.getMessage());
			return null;
		} catch (Exception e) {
			System.err.println("GherkinStructureCreator: Unexpected error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Read content from stream with proper encoding
	 */
	private String readContent(InputStream stream, IStreamContentAccessor accessor) throws IOException, CoreException {
		String charset = StandardCharsets.UTF_8.name();
		if (accessor instanceof IEncodedStreamContentAccessor) {
			String encoding = ((IEncodedStreamContentAccessor) accessor).getCharset();
			if (encoding != null) {
				charset = encoding;
			}
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

	/**
	 * Create hierarchical structure from Gherkin content
	 */
	private IStructureComparator createStructure(String content, Object input) {
		IDocument document = new Document(content);
		GherkinEditorDocument gherkinDoc = GherkinEditorDocument.parse(document, () -> {
			if (input instanceof IResourceProvider rp) {
				return rp.getResource();
			}
			return null;
		});

		Optional<Feature> featureOpt = gherkinDoc.getFeature();
		if (!featureOpt.isPresent()) {
			// Return an empty root if no feature found
			return new GherkinNode(null, 0, "Feature File", document, 0, content.length(), 
					GherkinNode.FEATURE_FILE);
		}

		Feature feature = featureOpt.get();

		// Create feature node as the root - no wrapper needed
		int featureStart = getOffset(document, feature.getLocation());
		int featureEnd = content.length();
		String featureName = feature.getName() != null && !feature.getName().isEmpty() ? feature.getName() : "Unnamed";
		String featureId = "Feature: " + featureName;
		GherkinNode featureNode = new GherkinNode(null, 1, featureId, document, 
				featureStart, featureEnd - featureStart, GherkinNode.FEATURE);

		// Process feature children (Background, Scenarios, Rules)
		for (FeatureChild child : feature.getChildren()) {
			processFeatureChild(featureNode, child, document);
		}

		return featureNode;
	}

	/**
	 * Process a feature child element (Background, Scenario, or Rule)
	 */
	private void processFeatureChild(GherkinNode parent, FeatureChild child, IDocument document) {
		// Background
		Optional<Background> bgOpt = child.getBackground();
		if (bgOpt.isPresent()) {
			Background bg = bgOpt.get();
			int start = getOffset(document, bg.getLocation());
			int end = getEndOffset(document, bg.getSteps(), document);
			String name = bg.getName() != null && !bg.getName().isEmpty() ? bg.getName() : "Unnamed";
			// Include line number to make ID unique
			int line = bg.getLocation().getLine().intValue();
			String id = "Background: " + name + " [line " + line + "]";
			GherkinNode bgNode = new GherkinNode(parent, 2, id, document, start, end - start, 
					GherkinNode.BACKGROUND);

			// Add steps as children
			for (Step step : bg.getSteps()) {
				addStepNode(bgNode, step, document);
			}
		}

		// Scenario
		Optional<Scenario> scenarioOpt = child.getScenario();
		if (scenarioOpt.isPresent()) {
			Scenario scenario = scenarioOpt.get();
			int start = getOffset(document, scenario.getLocation());
			int end = getEndOffset(document, scenario, document);
			String name = scenario.getName() != null && !scenario.getName().isEmpty() ? scenario.getName() : "Unnamed";
			String keyword = scenario.getKeyword() != null ? scenario.getKeyword().trim() : "Scenario";
			// Include line number to make ID unique
			int line = scenario.getLocation().getLine().intValue();
			String id = keyword + ": " + name + " [line " + line + "]";
			GherkinNode scenarioNode = new GherkinNode(parent, 3, id, document, start, end - start, 
					GherkinNode.SCENARIO);

			// Add steps as children
			for (Step step : scenario.getSteps()) {
				addStepNode(scenarioNode, step, document);
			}

			// Add examples if present
			for (Examples examples : scenario.getExamples()) {
				addExamplesNode(scenarioNode, examples, document);
			}
		}

		// Rule
		Optional<Rule> ruleOpt = child.getRule();
		if (ruleOpt.isPresent()) {
			Rule rule = ruleOpt.get();
			int start = getOffset(document, rule.getLocation());
			int end = getEndOffsetForRule(document, rule, document);
			String name = rule.getName() != null && !rule.getName().isEmpty() ? rule.getName() : "Unnamed";
			// Include line number to make ID unique
			int line = rule.getLocation().getLine().intValue();
			String id = "Rule: " + name + " [line " + line + "]";
			GherkinNode ruleNode = new GherkinNode(parent, 4, id, document, start, end - start, 
					GherkinNode.RULE);

			// Process rule children
			for (RuleChild ruleChild : rule.getChildren()) {
				processRuleChild(ruleNode, ruleChild, document);
			}
		}
	}

	/**
	 * Process a rule child element (Background or Scenario)
	 */
	private void processRuleChild(GherkinNode parent, RuleChild child, IDocument document) {
		// Background
		Optional<Background> bgOpt = child.getBackground();
		if (bgOpt.isPresent()) {
			Background bg = bgOpt.get();
			int start = getOffset(document, bg.getLocation());
			int end = getEndOffset(document, bg.getSteps(), document);
			String name = bg.getName() != null && !bg.getName().isEmpty() ? bg.getName() : "Unnamed";
			// Include line number to make ID unique
			int line = bg.getLocation().getLine().intValue();
			String id = "Background: " + name + " [line " + line + "]";
			GherkinNode bgNode = new GherkinNode(parent, 2, id, document, start, end - start, 
					GherkinNode.BACKGROUND);

			// Add steps as children
			for (Step step : bg.getSteps()) {
				addStepNode(bgNode, step, document);
			}
		}

		// Scenario
		Optional<Scenario> scenarioOpt = child.getScenario();
		if (scenarioOpt.isPresent()) {
			Scenario scenario = scenarioOpt.get();
			int start = getOffset(document, scenario.getLocation());
			int end = getEndOffset(document, scenario, document);
			String name = scenario.getName() != null && !scenario.getName().isEmpty() ? scenario.getName() : "Unnamed";
			String keyword = scenario.getKeyword() != null ? scenario.getKeyword().trim() : "Scenario";
			// Include line number to make ID unique
			int line = scenario.getLocation().getLine().intValue();
			String id = keyword + ": " + name + " [line " + line + "]";
			GherkinNode scenarioNode = new GherkinNode(parent, 3, id, document, start, end - start, 
					GherkinNode.SCENARIO);

			// Add steps as children
			for (Step step : scenario.getSteps()) {
				addStepNode(scenarioNode, step, document);
			}

			// Add examples if present
			for (Examples examples : scenario.getExamples()) {
				addExamplesNode(scenarioNode, examples, document);
			}
		}
	}

	/**
	 * Add an examples node
	 */
	private void addExamplesNode(GherkinNode parent, Examples examples, IDocument document) {
		int start = getOffset(document, examples.getLocation());
		int end = getEndOffsetForExamples(document, examples, document);
		String name = examples.getName() != null && !examples.getName().isEmpty() ? examples.getName() : "Unnamed";
		// Include line number to make ID unique
		int line = examples.getLocation().getLine().intValue();
		String id = "Examples: " + name + " [line " + line + "]";
		new GherkinNode(parent, 5, id, document, start, end - start, GherkinNode.EXAMPLES);
	}

	/**
	 * Add a step node
	 */
	private void addStepNode(GherkinNode parent, Step step, IDocument document) {
		int start = getOffset(document, step.getLocation());
		int end = getEndOffsetForStep(document, step, document);
		String stepText = step.getKeyword() + step.getText();
		// Include line number to make ID unique
		int line = step.getLocation().getLine().intValue();
		String id = stepText + " [line " + line + "]";
		new GherkinNode(parent, 6, id, document, start, end - start, GherkinNode.STEP);
	}

	/**
	 * Get character offset from location
	 */
	private int getOffset(IDocument document, Location location) {
		try {
			int line = location.getLine().intValue() - 1;
			int column = location.getColumn().orElse(1L).intValue() - 1;
			return document.getLineOffset(line) + column;
		} catch (BadLocationException e) {
			System.err.println("GherkinStructureCreator: Invalid location - line: " + 
				location.getLine() + ", column: " + location.getColumn().orElse(0L));
			return 0;
		}
	}

	/**
	 * Get end offset for a list of steps
	 */
	private int getEndOffset(IDocument document, List<Step> steps, IDocument doc) {
		if (steps.isEmpty()) {
			return document.getLength();
		}
		Step lastStep = steps.get(steps.size() - 1);
		return getEndOffsetForStep(document, lastStep, doc);
	}

	/**
	 * Get end offset for a scenario (including examples)
	 */
	private int getEndOffset(IDocument document, Scenario scenario, IDocument doc) {
		List<Examples> examplesList = scenario.getExamples();
		if (!examplesList.isEmpty()) {
			Examples lastExample = examplesList.get(examplesList.size() - 1);
			return getEndOffsetForExamples(document, lastExample, doc);
		}

		List<Step> steps = scenario.getSteps();
		if (!steps.isEmpty()) {
			return getEndOffset(document, steps, doc);
		}

		// Just the scenario line
		try {
			int line = scenario.getLocation().getLine().intValue() - 1;
			int offset = document.getLineOffset(line);
			int length = document.getLineLength(line);
			return offset + length;
		} catch (BadLocationException e) {
			System.err.println("GherkinStructureCreator: Invalid location in scenario: " + scenario.getName());
			return document.getLength();
		}
	}

	/**
	 * Get end offset for a step
	 */
	private int getEndOffsetForStep(IDocument document, Step step, IDocument doc) {
		try {
			int line = step.getLocation().getLine().intValue() - 1;
			
			// Check if step has a data table or doc string
			if (step.getDataTable().isPresent()) {
				List<TableRow> rows = step.getDataTable().get().getRows();
				if (!rows.isEmpty()) {
					TableRow lastRow = rows.get(rows.size() - 1);
					line = lastRow.getLocation().getLine().intValue() - 1;
				}
			} else if (step.getDocString().isPresent()) {
				Location docStringLoc = step.getDocString().get().getLocation();
				line = docStringLoc.getLine().intValue() - 1;
				// Doc strings span multiple lines, find the closing delimiter
				String delimiter = step.getDocString().get().getDelimiter();
				for (int i = line + 1; i < document.getNumberOfLines(); i++) {
					String lineContent = document.get(document.getLineOffset(i), document.getLineLength(i));
					if (lineContent.trim().equals(delimiter)) {
						line = i;
						break;
					}
				}
			}
			
			int offset = document.getLineOffset(line);
			int length = document.getLineLength(line);
			return offset + length;
		} catch (BadLocationException e) {
			System.err.println("GherkinStructureCreator: Invalid location in step: " + step.getText());
			return document.getLength();
		}
	}

	/**
	 * Get end offset for examples
	 */
	private int getEndOffsetForExamples(IDocument document, Examples examples, IDocument doc) {
		try {
			List<TableRow> tableBody = examples.getTableBody();
			if (!tableBody.isEmpty()) {
				TableRow lastRow = tableBody.get(tableBody.size() - 1);
				int line = lastRow.getLocation().getLine().intValue() - 1;
				int offset = document.getLineOffset(line);
				int length = document.getLineLength(line);
				return offset + length;
			}

			// Just header
			Optional<TableRow> headerOpt = examples.getTableHeader();
			if (headerOpt.isPresent()) {
				int line = headerOpt.get().getLocation().getLine().intValue() - 1;
				int offset = document.getLineOffset(line);
				int length = document.getLineLength(line);
				return offset + length;
			}

			// Just examples keyword line
			int line = examples.getLocation().getLine().intValue() - 1;
			int offset = document.getLineOffset(line);
			int length = document.getLineLength(line);
			return offset + length;
		} catch (BadLocationException e) {
			System.err.println("GherkinStructureCreator: Invalid location in examples");
			return document.getLength();
		}
	}

	/**
	 * Get end offset for a rule
	 */
	private int getEndOffsetForRule(IDocument document, Rule rule, IDocument doc) {
		List<RuleChild> children = rule.getChildren();
		if (children.isEmpty()) {
			try {
				int line = rule.getLocation().getLine().intValue() - 1;
				int offset = document.getLineOffset(line);
				int length = document.getLineLength(line);
				return offset + length;
			} catch (BadLocationException e) {
				System.err.println("GherkinStructureCreator: Invalid location in rule: " + 
					(rule.getName() != null ? rule.getName() : "unnamed"));
				return document.getLength();
			}
		}

		// Find last child
		RuleChild lastChild = children.get(children.size() - 1);
		if (lastChild.getScenario().isPresent()) {
			return getEndOffset(document, lastChild.getScenario().get(), doc);
		} else if (lastChild.getBackground().isPresent()) {
			Background bg = lastChild.getBackground().get();
			return getEndOffset(document, bg.getSteps(), doc);
		}

		return document.getLength();
	}

	@Override
	public IStructureComparator locate(Object path, Object input) {
		// Not implemented - Eclipse will use default behavior
		return null;
	}

	@Override
	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof GherkinNode) {
			GherkinNode gherkinNode = (GherkinNode) node;
			try {
				IDocument doc = gherkinNode.getDocument();
				if (doc != null) {
					int start = gherkinNode.getRange().getOffset();
					int length = gherkinNode.getRange().getLength();
					String content = doc.get(start, length);
					if (ignoreWhitespace) {
						return content.trim();
					}
					return content;
				}
			} catch (Exception e) {
				System.err.println("GherkinStructureCreator: Error getting contents: " + e.getMessage());
			}
		}
		return "";
	}

	@Override
	public void save(IStructureComparator node, Object input) {
		// Not implemented - compare is read-only
	}
}