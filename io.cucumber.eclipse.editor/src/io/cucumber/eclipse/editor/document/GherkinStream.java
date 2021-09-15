package io.cucumber.eclipse.editor.document;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Background;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step.DataTable;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Tag;
import io.cucumber.messages.Messages.ParseError;

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
		return getGherkinDocument().filter(GherkinDocument::hasFeature).map(GherkinDocument::getFeature);
	}

	public Optional<GherkinDocument> getGherkinDocument() {
		return Arrays.stream(envelopes).filter(Envelope::hasGherkinDocument).map(s -> s.getGherkinDocument())
				.findFirst();
	}

	public Stream<FeatureChild> getFeatureChilds() {
		return getFeature().stream().flatMap(GherkinStream::featureChilds);
	}

	public static Stream<FeatureChild> featureChilds(Feature feature) {
		return feature.getChildrenList().stream();
	}

	public Stream<Scenario> getScenarios() {
		return getFeatureChilds().filter(FeatureChild::hasScenario).map(FeatureChild::getScenario);
	}

	public static Stream<Scenario> scenarios(Feature feature) {
		return featureChilds(feature).filter(FeatureChild::hasScenario).map(FeatureChild::getScenario);
	}

	public Stream<Step> getSteps() {
		Stream<Step> backgroundSteps = getBackgrounds().flatMap(bg -> bg.getStepsList().stream());
		Stream<Step> scenarioSteps = getScenarios().flatMap(GherkinStream::scenarioSteps);
		return Stream.concat(scenarioSteps, backgroundSteps).distinct();
	}

	public static Stream<Step> scenarioSteps(Scenario scenario) {
		return scenario.getStepsList().stream();
	}

	public Stream<Background> getBackgrounds() {
		return getFeatureChilds().filter(FeatureChild::hasBackground).map(FeatureChild::getBackground);
	}

	public Stream<Tag> getTags() {

		return Stream.concat(getExamples().flatMap(example -> example.getTagsList().stream()),
				Stream.concat(getScenarios().flatMap(scenario -> scenario.getTagsList().stream()),
						getFeature().stream().flatMap(feature -> feature.getTagsList().stream())))
				.distinct();
	}

	public Stream<Examples> getExamples() {
		return getScenarios().flatMap(s -> s.getExamplesList().stream());
	}

	public Stream<TableRow> getTableHeaders() {
		return getExamples().filter(Examples::hasTableHeader).map(Examples::getTableHeader).distinct();
	}

	public Stream<List<TableRow>> getTableBodys() {
		return getExamples().filter(Examples::hasTableHeader).map(Examples::getTableBodyList).distinct();
	}

	public Stream<DataTable> getDataTables() {
		return getScenarios().flatMap(scenario -> scenario.getStepsList().stream()).filter(Step::hasDataTable)
				.map(Step::getDataTable).distinct();
	}

	/**
	 * @return a stream of parse errors for the given document
	 */
	public Stream<ParseError> getParseError() {
		return Arrays.stream(envelopes).filter(Envelope::hasParseError).map(Envelope::getParseError);
	}


}
