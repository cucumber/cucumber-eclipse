package io.cucumber.eclipse.editor.document;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.ParseError;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;

/**
 * Provides access to parsed Gherkin document elements from Cucumber message envelopes.
 * <p>
 * This class wraps one or more {@link Envelope} instances from the Cucumber Gherkin parser
 * and provides convenient stream-based access to the document structure including:
 * <ul>
 * <li>Features and scenarios</li>
 * <li>Steps, backgrounds, and examples</li>
 * <li>Tags and data tables</li>
 * <li>Parse errors</li>
 * </ul>
 * </p>
 * <p>
 * This class is extended by {@link GherkinEditorDocument} to provide Eclipse-specific
 * functionality like position mapping and keyword access.
 * </p>
 * 
 * @author christoph
 */
public class GherkinStream {

	private final Envelope[] envelopes;

	public GherkinStream(Envelope... envelopes) {
		this.envelopes = envelopes;
	}

	/**
	 * 
	 * @return the {@link Feature} of the document or an empty optional if no
	 *         feature is present (either none is defined or there are parse errors)
	 */
	public Optional<Feature> getFeature() {
		return getGherkinDocument().flatMap(GherkinDocument::getFeature);
	}

	/**
	 * @return the {@link GherkinDocument} of the stream or an empty optional if no
	 *         document is present
	 */
	public Optional<GherkinDocument> getGherkinDocument() {
		return Arrays.stream(envelopes).map(s -> s.getGherkinDocument()).filter(o -> o.isPresent()).map(o -> o.get())
				.findFirst();
	}

	/**
	 * @return a stream of all feature children (scenarios, rules, backgrounds)
	 */
	public Stream<FeatureChild> getFeatureChilds() {
		return getFeature().stream().flatMap(GherkinStream::featureChilds);
	}

	/**
	 * Returns a stream of feature children for the given feature.
	 * 
	 * @param feature the feature to get children from
	 * @return a stream of feature children
	 */
	public static Stream<FeatureChild> featureChilds(Feature feature) {
		return feature.getChildren().stream();
	}

	/**
	 * @return a stream of all scenarios in the document
	 */
	public Stream<Scenario> getScenarios() {
		return getFeatureChilds().map(FeatureChild::getScenario).filter(o -> o.isPresent()).map(o -> o.get());
	}

	/**
	 * Returns a stream of all scenarios in the given feature.
	 * 
	 * @param feature the feature to get scenarios from
	 * @return a stream of scenarios
	 */
	public static Stream<Scenario> scenarios(Feature feature) {
		return featureChilds(feature).map(FeatureChild::getScenario).filter(o -> o.isPresent()).map(o -> o.get());
	}

	/**
	 * Returns all steps in the document including both scenario steps and background steps.
	 * The returned stream contains distinct steps only.
	 * 
	 * @return a stream of all steps in the document
	 */
	public Stream<Step> getSteps() {
		Stream<Step> backgroundSteps = getBackgrounds().flatMap(bg -> bg.getSteps().stream());
		Stream<Step> scenarioSteps = getScenarios().flatMap(GherkinStream::scenarioSteps);
		return Stream.concat(scenarioSteps, backgroundSteps).distinct();
	}

	/**
	 * Returns a stream of all steps in the given scenario.
	 * 
	 * @param scenario the scenario to get steps from
	 * @return a stream of steps
	 */
	public static Stream<Step> scenarioSteps(Scenario scenario) {
		return scenario.getSteps().stream();
	}

	/**
	 * @return a stream of all backgrounds in the document
	 */
	public Stream<Background> getBackgrounds() {
		return getFeatureChilds().map(FeatureChild::getBackground).filter(o -> o.isPresent()).map(o -> o.get());
	}

	/**
	 * Returns all tags in the document from features, scenarios, and examples.
	 * The returned stream contains distinct tags only.
	 * 
	 * @return a stream of all tags in the document
	 */
	public Stream<Tag> getTags() {
		return Stream.concat(getExamples().flatMap(example -> example.getTags().stream()),
				Stream.concat(getScenarios().flatMap(scenario -> scenario.getTags().stream()),
						getFeature().stream().flatMap(feature -> feature.getTags().stream())))
				.distinct();
	}

	/**
	 * @return a stream of all examples in the document
	 */
	public Stream<Examples> getExamples() {
		return getScenarios().flatMap(s -> s.getExamples().stream());
	}

	/**
	 * Returns all example table headers from the document.
	 * The returned stream contains distinct headers only.
	 * 
	 * @return a stream of table headers
	 */
	public Stream<TableRow> getTableHeaders() {
		return getExamples().map(Examples::getTableHeader).filter(o -> o.isPresent()).map(o -> o.get()).distinct();
	}

	/**
	 * Returns all example table bodies from the document.
	 * Only includes examples that have a table header defined.
	 * The returned stream contains distinct bodies only.
	 * 
	 * @return a stream of table body lists
	 */
	public Stream<List<TableRow>> getTableBodys() {
		return getExamples().filter(e->e.getTableHeader().isPresent()).map(Examples::getTableBody).distinct();
	}

	/**
	 * Returns all data tables attached to steps in the document.
	 * The returned stream contains distinct data tables only.
	 * 
	 * @return a stream of data tables
	 */
	public Stream<DataTable> getDataTables() {
		return getScenarios().flatMap(scenario -> scenario.getSteps().stream())
				.map(Step::getDataTable).filter(o -> o.isPresent()).map(o -> o.get()).distinct();
	}

	/**
	 * @return a stream of parse errors for the given document
	 */
	public Stream<ParseError> getParseError() {
		return Arrays.stream(envelopes).map(Envelope::getParseError).filter(o -> o.isPresent()).map(o -> o.get());
	}


}
