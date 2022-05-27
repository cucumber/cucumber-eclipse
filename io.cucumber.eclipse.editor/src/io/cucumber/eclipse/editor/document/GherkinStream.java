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
 * Helper handling the different items of a message stream
 * 
 * @author christoph
 *
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

	public Optional<GherkinDocument> getGherkinDocument() {
		return Arrays.stream(envelopes).map(s -> s.getGherkinDocument()).filter(o -> o.isPresent()).map(o -> o.get())
				.findFirst();
	}

	public Stream<FeatureChild> getFeatureChilds() {
		return getFeature().stream().flatMap(GherkinStream::featureChilds);
	}

	public static Stream<FeatureChild> featureChilds(Feature feature) {
		return feature.getChildren().stream();
	}

	public Stream<Scenario> getScenarios() {
		return getFeatureChilds().map(FeatureChild::getScenario).filter(o -> o.isPresent()).map(o -> o.get());
	}

	public static Stream<Scenario> scenarios(Feature feature) {
		return featureChilds(feature).map(FeatureChild::getScenario).filter(o -> o.isPresent()).map(o -> o.get());
	}

	public Stream<Step> getSteps() {
		Stream<Step> backgroundSteps = getBackgrounds().flatMap(bg -> bg.getSteps().stream());
		Stream<Step> scenarioSteps = getScenarios().flatMap(GherkinStream::scenarioSteps);
		return Stream.concat(scenarioSteps, backgroundSteps).distinct();
	}

	public static Stream<Step> scenarioSteps(Scenario scenario) {
		return scenario.getSteps().stream();
	}

	public Stream<Background> getBackgrounds() {
		return getFeatureChilds().map(FeatureChild::getBackground).filter(o -> o.isPresent()).map(o -> o.get());
	}

	public Stream<Tag> getTags() {
		return Stream.concat(getExamples().flatMap(example -> example.getTags().stream()),
				Stream.concat(getScenarios().flatMap(scenario -> scenario.getTags().stream()),
						getFeature().stream().flatMap(feature -> feature.getTags().stream())))
				.distinct();
	}

	public Stream<Examples> getExamples() {
		return getScenarios().flatMap(s -> s.getExamples().stream());
	}

	public Stream<TableRow> getTableHeaders() {
		return getExamples().map(Examples::getTableHeader).filter(o -> o.isPresent()).map(o -> o.get()).distinct();
	}

	public Stream<List<TableRow>> getTableBodys() {
		return getExamples().map(Examples::getTableBody).distinct();
	}

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
