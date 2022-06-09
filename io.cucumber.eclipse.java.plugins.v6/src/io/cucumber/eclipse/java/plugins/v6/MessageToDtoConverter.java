package io.cucumber.eclipse.java.plugins.v6;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.cucumber.eclipse.java.plugins.dto.Attachment;
import io.cucumber.eclipse.java.plugins.dto.Duration;
import io.cucumber.eclipse.java.plugins.dto.Envelope;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Background;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Comment;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.DataTable;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.DocString;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Examples;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Feature;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.FeatureChild;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Rule;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.RuleChild;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Scenario;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Step;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.TableCell;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.TableRow;
import io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Tag;
import io.cucumber.eclipse.java.plugins.dto.Hook;
import io.cucumber.eclipse.java.plugins.dto.Location;
import io.cucumber.eclipse.java.plugins.dto.Meta;
import io.cucumber.eclipse.java.plugins.dto.Meta.Ci;
import io.cucumber.eclipse.java.plugins.dto.Meta.Git;
import io.cucumber.eclipse.java.plugins.dto.Meta.Product;
import io.cucumber.eclipse.java.plugins.dto.ParameterType;
import io.cucumber.eclipse.java.plugins.dto.ParseError;
import io.cucumber.eclipse.java.plugins.dto.Pickle;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleDocString;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleStep;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleStepArgument;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTable;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTableCell;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTableRow;
import io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTag;
import io.cucumber.eclipse.java.plugins.dto.Source;
import io.cucumber.eclipse.java.plugins.dto.SourceReference;
import io.cucumber.eclipse.java.plugins.dto.SourceReference.JavaMethod;
import io.cucumber.eclipse.java.plugins.dto.SourceReference.JavaStackTraceElement;
import io.cucumber.eclipse.java.plugins.dto.StepDefinition;
import io.cucumber.eclipse.java.plugins.dto.StepDefinition.StepDefinitionPattern;
import io.cucumber.eclipse.java.plugins.dto.TestCase;
import io.cucumber.eclipse.java.plugins.dto.TestCase.Group;
import io.cucumber.eclipse.java.plugins.dto.TestCase.StepMatchArgument;
import io.cucumber.eclipse.java.plugins.dto.TestCase.StepMatchArgumentsList;
import io.cucumber.eclipse.java.plugins.dto.TestCase.TestStep;
import io.cucumber.eclipse.java.plugins.dto.TestCaseFinished;
import io.cucumber.eclipse.java.plugins.dto.TestCaseStarted;
import io.cucumber.eclipse.java.plugins.dto.TestRunFinished;
import io.cucumber.eclipse.java.plugins.dto.TestRunStarted;
import io.cucumber.eclipse.java.plugins.dto.TestStepFinished;
import io.cucumber.eclipse.java.plugins.dto.TestStepFinished.TestStepResult;
import io.cucumber.eclipse.java.plugins.dto.TestStepStarted;
import io.cucumber.eclipse.java.plugins.dto.Timestamp;
import io.cucumber.eclipse.java.plugins.dto.UndefinedParameterType;

public class MessageToDtoConverter {

	/**
	 * @param d
	 * @return
	 */
	public static Envelope convert(io.cucumber.messages.Messages.Envelope d) {
		if (d == null)
			return null;
		return new Envelope(d.hasAttachment() ? convert(d.getAttachment()) : null,
				d.hasGherkinDocument() ? convert(d.getGherkinDocument()) : null,
				d.hasHook() ? convert(d.getHook()) : null, d.hasMeta() ? convert(d.getMeta()) : null,
				d.hasParameterType() ? convert(d.getParameterType()) : null,
				d.hasParseError() ? convert(d.getParseError()) : null, d.hasPickle() ? convert(d.getPickle()) : null,
				d.hasSource() ? convert(d.getSource()) : null,
				d.hasStepDefinition() ? convert(d.getStepDefinition()) : null,
				d.hasTestCase() ? convert(d.getTestCase()) : null,
				d.hasTestCaseFinished() ? convert(d.getTestCaseFinished()) : null,
				d.hasTestCaseStarted() ? convert(d.getTestCaseStarted()) : null,
				d.hasTestRunFinished() ? convert(d.getTestRunFinished()) : null,
				d.hasTestRunStarted() ? convert(d.getTestRunStarted()) : null,
				d.hasTestStepFinished() ? convert(d.getTestStepFinished()) : null,
				d.hasTestStepStarted() ? convert(d.getTestStepStarted()) : null,
				d.hasUndefinedParameterType() ? convert(d.getUndefinedParameterType()) : null);
	}

	public static <T, K> List<K> convertList(List<T> list, Function<T, K> converter) {
		Optional<List<K>> map = Optional.ofNullable(list)
				.map(l -> l.stream().map(v -> converter.apply(v)).collect(toList()));
		return map.orElse(null);
	}

	public static <T> List<T> convertList(List<T> list) {
		if (list == null)
			return null;
		return new ArrayList<>(list);
	}

	public static UndefinedParameterType convert(io.cucumber.messages.Messages.UndefinedParameterType d) {
		if (d == null)
			return null;
		return new UndefinedParameterType(d.getExpression(), d.getName());
	}

	public static TestStepStarted convert(io.cucumber.messages.Messages.TestStepStarted d) {
		if (d == null)
			return null;
		return new TestStepStarted(d.getTestCaseStartedId(), d.getTestStepId(),
				d.hasTimestamp() ? convert(d.getTimestamp()) : null);
	}

	public static Timestamp convert(io.cucumber.messages.Messages.Timestamp d) {
		if (d == null)
			return null;
		return new Timestamp(d.getSeconds(), (long) d.getNanos());
	}

	public static TestStepFinished convert(io.cucumber.messages.Messages.TestStepFinished d) {
		if (d == null)
			return null;

		return new TestStepFinished(d.getTestCaseStartedId(), d.getTestStepId(),
				d.hasTestStepResult() ? convert(d.getTestStepResult()) : null,
				d.hasTimestamp() ? convert(d.getTimestamp()) : null);
	}

	public static TestStepResult convert(io.cucumber.messages.Messages.TestStepFinished.TestStepResult d) {
		if (d == null)
			return null;
		return new TestStepResult(d.hasDuration() ? convert(d.getDuration()) : null, d.getMessage(),
				d.getStatus().name());
	}

	public static Duration convert(io.cucumber.messages.Messages.Duration d) {
		if (d == null)
			return null;
		return new Duration(d.getSeconds(), (long) d.getNanos());
	}

	public static TestRunStarted convert(io.cucumber.messages.Messages.TestRunStarted d) {
		if (d == null)
			return null;
		return new TestRunStarted(d.hasTimestamp() ? convert(d.getTimestamp()) : null);
	}

	public static TestRunFinished convert(io.cucumber.messages.Messages.TestRunFinished d) {
		if (d == null)
			return null;
		return new TestRunFinished(d.getMessage(), d.getSuccess(), d.hasTimestamp() ? convert(d.getTimestamp()) : null);
	}

	public static TestCaseStarted convert(io.cucumber.messages.Messages.TestCaseStarted d) {
		if (d == null)
			return null;
		return new TestCaseStarted((long) d.getAttempt(), d.getId(), d.getTestCaseId(),
				d.hasTimestamp() ? convert(d.getTimestamp()) : null);
	}

	public static TestCaseFinished convert(io.cucumber.messages.Messages.TestCaseFinished d) {
		if (d == null)
			return null;
		return new TestCaseFinished(d.getTestCaseStartedId(), d.hasTimestamp() ? convert(d.getTimestamp()) : null,
				/* d.getWillBeRetried() */false);
	}

	public static TestCase convert(io.cucumber.messages.Messages.TestCase d) {
		if (d == null)
			return null;
		return new TestCase(d.getId(), d.getPickleId(),
				convertList(d.getTestStepsList(), MessageToDtoConverter::convert));
	}

	public static TestStep convert(io.cucumber.messages.Messages.TestCase.TestStep d) {
		if (d == null)
			return null;
		return new TestStep(d.getHookId(), d.getId(), d.getPickleStepId(), convertList(d.getStepDefinitionIdsList()),
				convertList(d.getStepMatchArgumentsListsList(), MessageToDtoConverter::convert));
	}

	public static StepMatchArgumentsList convert(
			io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList d) {
		if (d == null)
			return null;
		return new StepMatchArgumentsList(convertList(d.getStepMatchArgumentsList(), MessageToDtoConverter::convert));
	}

	public static StepMatchArgument convert(
			io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList.StepMatchArgument d) {
		if (d == null)
			return null;
		return new StepMatchArgument(d.hasGroup() ? convert(d.getGroup()) : null, d.getParameterTypeName());
	}

	public static Group convert(
			io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList.StepMatchArgument.Group d) {
		if (d == null)
			return null;
		return new Group(convertList(convertList(d.getChildrenList()), MessageToDtoConverter::convert),
				(long) d.getStart(), d.getValue());
	}

	public static StepDefinition convert(io.cucumber.messages.Messages.StepDefinition d) {
		if (d == null)
			return null;

		return new StepDefinition(d.getId(), d.hasPattern() ? convert(d.getPattern()) : null,
				d.hasSourceReference() ? convert(d.getSourceReference()) : null);
	}

	public static StepDefinitionPattern convert(io.cucumber.messages.Messages.StepDefinition.StepDefinitionPattern d) {
		if (d == null)
			return null;
		return new StepDefinitionPattern(d.getSource(), d.getType().name());
	}

	public static Source convert(io.cucumber.messages.Messages.Source d) {
		if (d == null)
			return null;
		return new Source(d.getUri(), d.getData(), d.getMediaType());
	}

	public static Pickle convert(io.cucumber.messages.Messages.Pickle d) {
		if (d == null)
			return null;
		return new Pickle(d.getId(), d.getUri(), d.getName(), d.getLanguage(),
				convertList(d.getStepsList(), MessageToDtoConverter::convert),
				convertList(d.getTagsList(), MessageToDtoConverter::convert), convertList(d.getAstNodeIdsList()));
	}

	public static PickleTag convert(io.cucumber.messages.Messages.Pickle.PickleTag d) {
		if (d == null)
			return null;
		return new PickleTag(d.getName(), d.getAstNodeId());
	}

	public static PickleStep convert(io.cucumber.messages.Messages.Pickle.PickleStep d) {
		if (d == null)
			return null;
		return new PickleStep(d.hasArgument() ? convert(d.getArgument()) : null, convertList(d.getAstNodeIdsList()),
				d.getId(), d.getText(), null);
	}

	public static PickleStepArgument convert(io.cucumber.messages.Messages.PickleStepArgument d) {
		if (d == null)
			return null;

		return new PickleStepArgument(d.hasDocString() ? convert(d.getDocString()) : null,
				d.hasDataTable() ? convert(d.getDataTable()) : null);
	}

	public static PickleTable convert(io.cucumber.messages.Messages.PickleStepArgument.PickleTable d) {
		if (d == null)
			return null;
		return new PickleTable(convertList(d.getRowsList(), MessageToDtoConverter::convert));
	}

	public static PickleTableRow convert(
			io.cucumber.messages.Messages.PickleStepArgument.PickleTable.PickleTableRow d) {
		if (d == null)
			return null;
		return new PickleTableRow(convertList(d.getCellsList(), MessageToDtoConverter::convert));
	}

	public static PickleTableCell convert(
			io.cucumber.messages.Messages.PickleStepArgument.PickleTable.PickleTableRow.PickleTableCell d) {
		if (d == null)
			return null;
		return new PickleTableCell(d.getValue());
	}

	public static PickleDocString convert(io.cucumber.messages.Messages.PickleStepArgument.PickleDocString d) {
		if (d == null)
			return null;
		return new PickleDocString(d.getMediaType(), d.getContent());
	}

	public static ParseError convert(io.cucumber.messages.Messages.ParseError d) {
		if (d == null)
			return null;
		return new ParseError(d.hasSource() ? convert(d.getSource()) : null, d.getMessage());
	}

	public static ParameterType convert(io.cucumber.messages.Messages.ParameterType d) {
		if (d == null)
			return null;
		return new ParameterType(d.getName(), convertList(d.getRegularExpressionsList()),
				d.getPreferForRegularExpressionMatch(), d.getUseForSnippets(), d.getId());
	}

	public static Meta convert(io.cucumber.messages.Messages.Meta d) {
		if (d == null)
			return null;

		return new Meta(d.getProtocolVersion(), d.hasImplementation() ? convert(d.getImplementation()) : null,
				d.hasRuntime() ? convert(d.getRuntime()) : null, d.hasOs() ? convert(d.getOs()) : null,
				d.hasCpu() ? convert(d.getCpu()) : null, d.hasCi() ? convert(d.getCi()) : null);
	}

	public static Ci convert(io.cucumber.messages.Messages.Meta.CI d) {
		if (d == null)
			return null;

		return new Ci(d.getName(), d.getUrl(), /* d.getBuildNumber() */"", d.hasGit() ? convert(d.getGit()) : null);
	}

	public static Git convert(io.cucumber.messages.Messages.Meta.CI.Git d) {
		if (d == null)
			return null;
		return new Git(d.getRemote(), d.getRevision(), d.getBranch(), d.getTag());
	}

	public static Product convert(io.cucumber.messages.Messages.Meta.Product d) {
		if (d == null)
			return null;
		return new Product(d.getName(), d.getVersion());
	}

	public static Hook convert(io.cucumber.messages.Messages.Hook d) {
		if (d == null)
			return null;
		return new Hook(d.getId(), /* d.getName() */d.getId(),
				d.hasSourceReference() ? convert(d.getSourceReference()) : null, d.getTagExpression());
	}

	public static SourceReference convert(io.cucumber.messages.Messages.SourceReference d) {
		if (d == null)
			return null;
		return new SourceReference(d.getUri(), d.hasJavaMethod() ? convert(d.getJavaMethod()) : null,
				d.hasJavaStackTraceElement() ? convert(d.getJavaStackTraceElement()) : null,
				d.hasLocation() ? convert(d.getLocation()) : null);
	}

	public static JavaMethod convert(io.cucumber.messages.Messages.SourceReference.JavaMethod d) {
		if (d == null)
			return null;
		return new JavaMethod(d.getClassName(), d.getMethodName(), convertList(d.getMethodParameterTypesList()));
	}

	public static JavaStackTraceElement convert(io.cucumber.messages.Messages.SourceReference.JavaStackTraceElement d) {
		if (d == null)
			return null;
		return new JavaStackTraceElement(d.getClassName(), d.getFileName(), d.getMethodName());
	}

	public static Attachment convert(io.cucumber.messages.Messages.Attachment d) {
		if (d == null)
			return null;
		return new Attachment(d.getBody(), d.getContentEncoding().name(), d.getFileName(), d.getMediaType(),
				d.hasSource() ? convert1(d.getSource()) : null, d.getTestCaseStartedId(), d.getTestStepId(),
				d.getUrl());
	}

	private static Source convert1(io.cucumber.messages.Messages.SourceReference d) {
		return new Source(d.getUri(), null, null);
	}

	public static Comment convert(io.cucumber.messages.Messages.GherkinDocument.Comment d) {
		if (d == null)
			return null;
		return new Comment(d.hasLocation() ? convert(d.getLocation()) : null, d.getText());
	}

	public static Feature convert(io.cucumber.messages.Messages.GherkinDocument.Feature d) {
		if (d == null)
			return null;
		return new Feature(d.hasLocation() ? convert(d.getLocation()) : null,
				convertList(d.getTagsList(), MessageToDtoConverter::convert), d.getLanguage(), d.getKeyword(),
				d.getName(), d.getDescription(), convertList(d.getChildrenList(), MessageToDtoConverter::convert));
	}

	public static Tag convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Tag d) {
		if (d == null)
			return null;
		return new Tag(d.hasLocation() ? convert(d.getLocation()) : null, d.getName(), d.getId());
	}

	public static FeatureChild convert(io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild d) {
		if (d == null)
			return null;
		return new FeatureChild(d.hasRule() ? convert(d.getRule()) : null,
				d.hasBackground() ? convert(d.getBackground()) : null,
				d.hasScenario() ? convert(d.getScenario()) : null);
	}

	public static Scenario convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario d) {
		if (d == null)
			return null;
		return new Scenario(convert(d.hasLocation() ? d.getLocation() : null),
				convertList(d.getTagsList(), MessageToDtoConverter::convert), d.getKeyword(), d.getName(),
				d.getDescription(), convertList(d.getStepsList(), MessageToDtoConverter::convert),
				convertList(d.getExamplesList(), MessageToDtoConverter::convert), d.getId());
	}

	public static Examples convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples d) {
		if (d == null)
			return null;
		return new Examples(d.hasLocation() ? convert(d.getLocation()) : null,
				convertList(d.getTagsList(), MessageToDtoConverter::convert), d.getKeyword(), d.getName(),
				d.getDescription(), d.hasTableHeader() ? convert(d.getTableHeader()) : null,
				convertList(d.getTableBodyList(), MessageToDtoConverter::convert), d.getId());
	}

	public static Rule convert(io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.Rule d) {
		if (d == null)
			return null;
		return new Rule(d.hasLocation() ? convert(d.getLocation()) : null,
//				convertList(d.getTags(), MessageToDtoConverter::convert), 
				List.of(), d.getKeyword(), d.getName(), d.getDescription(),
				convertList(d.getChildrenList(), MessageToDtoConverter::convert), d.getId());
	}

	public static RuleChild convert(io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild d) {
		if (d == null)
			return null;
		return new RuleChild(d.hasBackground() ? convert(d.getBackground()) : null,
				d.hasScenario() ? convert(d.getScenario()) : null);
	}

	public static Background convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Background d) {
		if (d == null)
			return null;
		return new Background(d.hasLocation() ? convert(d.getLocation()) : null, d.getKeyword(), d.getName(),
				d.getDescription(), convertList(d.getStepsList(), MessageToDtoConverter::convert), d.getId());
	}

	public static Step convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Step d) {
		if (d == null)
			return null;
		return new Step(d.hasLocation() ? convert(d.getLocation()) : null, d.getKeyword(),null, d.getText(),
				d.hasDocString() ? convert(d.getDocString()) : null,
				d.hasDataTable() ? convert(d.getDataTable()) : null, d.getId());
	}

	public static DocString convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Step.DocString d) {
		if (d == null)
			return null;
		return new DocString(d.hasLocation() ? convert(d.getLocation()) : null, d.getMediaType(), d.getContent(),
				d.getDelimiter());
	}

	public static DataTable convert(io.cucumber.messages.Messages.GherkinDocument.Feature.Step.DataTable d) {
		if (d == null)
			return null;
		return new DataTable(convert(d.hasLocation() ? d.getLocation() : null),
				convertList(d.getRowsList(), MessageToDtoConverter::convert));
	}

	public static TableRow convert(io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow d) {
		if (d == null)
			return null;
		return new TableRow(d.hasLocation() ? convert(d.getLocation()) : null,
				convertList(d.getCellsList(), MessageToDtoConverter::convert), d.getId());
	}

	public static TableCell convert(io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow.TableCell d) {
		if (d == null)
			return null;
		return new TableCell(d.hasLocation() ? convert(d.getLocation()) : null, d.getValue());
	}

	public static GherkinDocument convert(io.cucumber.messages.Messages.GherkinDocument d) {
		if (d == null)
			return null;
		return new GherkinDocument(d.getUri(),d.hasFeature()? convert(d.getFeature()):null,
				convertList(d.getCommentsList(), MessageToDtoConverter::convert));
	}

	public static Location convert(io.cucumber.messages.Messages.Location d) {
		if (d == null)
			return null;
		return new Location( (long) d.getLine(), (long) d.getColumn());
	}
}
