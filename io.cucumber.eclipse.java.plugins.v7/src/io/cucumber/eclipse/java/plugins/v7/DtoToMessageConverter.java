package io.cucumber.eclipse.java.plugins.v7;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


import io.cucumber.messages.types.Attachment;
import io.cucumber.messages.types.AttachmentContentEncoding;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Ci;
import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Duration;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Git;
import io.cucumber.messages.types.Group;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.JavaMethod;
import io.cucumber.messages.types.JavaStackTraceElement;
import io.cucumber.messages.types.Meta;
import io.cucumber.messages.types.ParameterType;
import io.cucumber.messages.types.ParseError;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleDocString;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleStepArgument;
import io.cucumber.messages.types.PickleTable;
import io.cucumber.messages.types.PickleTableCell;
import io.cucumber.messages.types.PickleTableRow;
import io.cucumber.messages.types.PickleTag;
import io.cucumber.messages.types.Product;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepDefinitionPattern;
import io.cucumber.messages.types.StepDefinitionPatternType;
import io.cucumber.messages.types.StepMatchArgument;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;
import io.cucumber.messages.types.TestCase;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.messages.types.TestStepStarted;
import io.cucumber.messages.types.Timestamp;
import io.cucumber.messages.types.UndefinedParameterType;

public class DtoToMessageConverter {

	public static Envelope convert(io.cucumber.eclipse.java.plugins.dto.Envelope d) {
		if (d == null)
			return null;
		return new io.cucumber.messages.types.Envelope(convert(d.attachment), convert(d.gherkinDocument),
				convert(d.hook), convert(d.meta), convert(d.parameterType), convert(d.parseError), convert(d.pickle),
				convert(d.source), convert(d.stepDefinition), convert(d.testCase), convert(d.testCaseFinished),
				convert(d.testCaseStarted), convert(d.testRunFinished), convert(d.testRunStarted),
				convert(d.testStepFinished), convert(d.testStepStarted), convert(d.undefinedParameterType));
	}

	public static <T, K> List<K> convertList(List<T> list, Function<T, K> converter) {
		Optional<List<K>> map = Optional.ofNullable(list)
				.map(l -> l.stream().map(v -> converter.apply(v)).collect(toList()));
		return map.orElse(null);
	}

	public static UndefinedParameterType convert(io.cucumber.eclipse.java.plugins.dto.UndefinedParameterType d) {
		if (d == null)
			return null;
		return new UndefinedParameterType(d.expression, d.name);
	}

	public static TestStepStarted convert(io.cucumber.eclipse.java.plugins.dto.TestStepStarted d) {
		if (d == null)
			return null;
		return new TestStepStarted(d.testCaseStartedId, d.testStepId, convert(d.timestamp));
	}

	public static Timestamp convert(io.cucumber.eclipse.java.plugins.dto.Timestamp d) {
		if (d == null)
			return null;
		return new Timestamp(d.seconds, d.nanos);
	}

	public static TestStepFinished convert(io.cucumber.eclipse.java.plugins.dto.TestStepFinished d) {
		if (d == null)
			return null;
		return new TestStepFinished(d.testCaseStartedId, d.testStepId, convert(d.testStepResult), convert(d.timestamp));
	}

	public static TestStepResult convert(io.cucumber.eclipse.java.plugins.dto.TestStepFinished.TestStepResult d) {
		if (d == null)
			return null;
		return new TestStepResult(convert(d.duration), d.message, TestStepResultStatus.fromValue(d.status));
	}

	public static Duration convert(io.cucumber.eclipse.java.plugins.dto.Duration d) {
		if (d == null)
			return null;
		return new Duration(d.seconds, d.nanos);
	}

	public static TestRunStarted convert(io.cucumber.eclipse.java.plugins.dto.TestRunStarted d) {
		if (d == null)
			return null;
		return new TestRunStarted(convert(d.timestamp));
	}

	public static TestRunFinished convert(io.cucumber.eclipse.java.plugins.dto.TestRunFinished d) {
		if (d == null)
			return null;
		return new TestRunFinished(d.message, d.success, convert(d.timestamp));
	}

	public static TestCaseStarted convert(io.cucumber.eclipse.java.plugins.dto.TestCaseStarted d) {
		if (d == null)
			return null;
		return new TestCaseStarted(d.attempt, d.id, d.testCaseId, convert(d.timestamp));
	}

	public static TestCaseFinished convert(io.cucumber.eclipse.java.plugins.dto.TestCaseFinished d) {
		if (d == null)
			return null;
		return new TestCaseFinished(d.testCaseStartedId, convert(d.timestamp), d.willBeRetried);
	}

	public static TestCase convert(io.cucumber.eclipse.java.plugins.dto.TestCase d) {
		if (d == null)
			return null;
		return new TestCase(d.id, d.pickleId, convertList(d.testSteps,DtoToMessageConverter::convert));
	}

	public static TestStep convert(io.cucumber.eclipse.java.plugins.dto.TestCase.TestStep d) {
		if (d == null)
			return null;
		return new TestStep(d.hookId, d.id, d.pickleStepId, d.stepDefinitionIds,
				convertList(d.stepMatchArgumentsLists, DtoToMessageConverter::convert));
	}

	public static StepMatchArgumentsList convert(io.cucumber.eclipse.java.plugins.dto.TestCase.StepMatchArgumentsList d) {
		if (d == null)
			return null;
		return new StepMatchArgumentsList(
				convertList(d.stepMatchArguments, DtoToMessageConverter::convert));
	}

	public static StepMatchArgument convert(io.cucumber.eclipse.java.plugins.dto.TestCase.StepMatchArgument d) {
		if (d == null)
			return null;
		return new StepMatchArgument(convert(d.group), d.parameterTypeName);
	}

	public static Group convert(io.cucumber.eclipse.java.plugins.dto.TestCase.Group d) {
		if (d == null)
			return null;
		return new Group(convertList(d.children,DtoToMessageConverter::convert), d.start, d.value);
	}

	public static StepDefinition convert(io.cucumber.eclipse.java.plugins.dto.StepDefinition d) {
		if (d == null)
			return null;
		return new StepDefinition(d.id, convert(d.pattern), convert(d.sourceReference));
	}

	public static StepDefinitionPattern convert(
			io.cucumber.eclipse.java.plugins.dto.StepDefinition.StepDefinitionPattern d) {
		if (d == null)
			return null;
		return new StepDefinitionPattern(d.source, StepDefinitionPatternType.fromValue(d.type));
	}

	public static Source convert(io.cucumber.eclipse.java.plugins.dto.Source d) {
		if (d == null)
			return null;
		return new Source(d.uri, d.data, io.cucumber.messages.types.SourceMediaType.fromValue(d.mediaType));
	}

	public static Pickle convert(io.cucumber.eclipse.java.plugins.dto.Pickle d) {
		if (d == null)
			return null;
		return new Pickle(d.id, d.uri, d.name, d.language, convertList(d.steps,DtoToMessageConverter::convert),
				convertList(d.tags,DtoToMessageConverter::convert), d.astNodeIds);
	}

	public static PickleTag convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTag d) {
		if (d == null)
			return null;
		return new PickleTag(d.name, d.astNodeId);
	}

	public static PickleStep convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleStep d) {
		if (d == null)
			return null;
		return new PickleStep(convert(d.argument), d.astNodeIds, d.id, /*PickleStepType.valueOf(d.type),*/ d.text);
	}

	public static PickleStepArgument convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleStepArgument d) {
		if (d == null)
			return null;
		return new PickleStepArgument(convert(d.docString), convert(d.dataTable));
	}

	public static PickleTable convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTable d) {
		if (d == null)
			return null;
		return new PickleTable(convertList(d.rows,DtoToMessageConverter::convert));
	}

	public static PickleTableRow convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTableRow d) {
		if (d == null)
			return null;
		return new PickleTableRow(convertList(d.cells,DtoToMessageConverter::convert));
	}

	public static PickleTableCell convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTableCell d) {
		if (d == null)
			return null;
		return new PickleTableCell(d.value);
	}

	public static PickleDocString convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleDocString d) {
		if (d == null)
			return null;
		return new PickleDocString(d.mediaType, d.content);
	}

	public static ParseError convert(io.cucumber.eclipse.java.plugins.dto.ParseError d) {
		if (d == null)
			return null;
		return new ParseError(convert(d.source), d.message);
	}

	public static ParameterType convert(io.cucumber.eclipse.java.plugins.dto.ParameterType d) {
		if (d == null)
			return null;
		return new ParameterType(d.name, d.regularExpressions, d.preferForRegularExpressionMatch, d.useForSnippets,
				d.id);
	}

	public static Meta convert(io.cucumber.eclipse.java.plugins.dto.Meta d) {
		if (d == null)
			return null;
		return new Meta(d.protocolVersion, convert(d.implementation), convert(d.runtime), convert(d.os), convert(d.cpu),
				convert(d.ci));
	}

	public static Ci convert(io.cucumber.eclipse.java.plugins.dto.Meta.Ci d) {
		if (d == null)
			return null;
		return new Ci(d.name, d.url, d.buildNumber, convert(d.git));
	}

	public static Git convert(io.cucumber.eclipse.java.plugins.dto.Meta.Git d) {
		if (d == null)
			return null;
		return new Git(d.remote, d.revision, d.branch, d.tag);
	}

	public static Product convert(io.cucumber.eclipse.java.plugins.dto.Meta.Product d) {
		if (d == null)
			return null;
		return new Product(d.name, d.version);
	}

	public static Hook convert(io.cucumber.eclipse.java.plugins.dto.Hook d) {
		if (d == null)
			return null;
		return new Hook(d.id, d.name, convert(d.sourceReference), d.tagExpression);
	}

	public static SourceReference convert(io.cucumber.eclipse.java.plugins.dto.SourceReference d) {
		if (d == null)
			return null;
		return new SourceReference(d.uri, convert(d.javaMethod), convert(d.javaStackTraceElement), convert(d.location));
	}

	public static JavaMethod convert(io.cucumber.eclipse.java.plugins.dto.SourceReference.JavaMethod d) {
		if (d == null)
			return null;
		return new JavaMethod(d.className, d.methodName, d.methodParameterTypes);
	}

	public static JavaStackTraceElement convert(
			io.cucumber.eclipse.java.plugins.dto.SourceReference.JavaStackTraceElement d) {
		if (d == null)
			return null;
		return new JavaStackTraceElement(d.className, d.fileName, d.methodName);
	}

	public static Attachment convert(io.cucumber.eclipse.java.plugins.dto.Attachment d) {
		if (d == null)
			return null;
		return new Attachment(d.body, 
				AttachmentContentEncoding.fromValue(d.contentEncoding), 
				d.fileName, d.mediaType, convert(d.source),
				d.testCaseStartedId, d.testStepId, d.url);
	}

	public static Comment convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Comment d) {
		if (d == null)
			return null;
		return new Comment(convert(d.location), d.text);
	}

	public static Feature convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Feature d) {
		if (d == null)
			return null;
		return new io.cucumber.messages.types.Feature(convert(d.location),
				convertList(d.tags,DtoToMessageConverter::convert), d.language, d.keyword, d.name, d.description,
				convertList(d.children,DtoToMessageConverter::convert));
	}

	public static Tag convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Tag d) {
		if (d == null)
			return null;
		return new Tag(convert(d.location), d.name, d.id);
	}

	public static FeatureChild convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.FeatureChild d) {
		if (d == null)
			return null;
		return new io.cucumber.messages.types.FeatureChild(convert(d.rule), convert(d.background), convert(d.scenario));
	}

	public static Scenario convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Scenario d) {
		if (d == null)
			return null;
		return new Scenario(convert(d.location),
				convertList(d.tags,DtoToMessageConverter::convert), d.keyword, d.name, d.description,
				convertList(d.steps,DtoToMessageConverter::convert),
				convertList(d.examples,DtoToMessageConverter::convert), d.id);
	}

	public static Examples convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Examples d) {
		if (d == null)
			return null;
		return new Examples(convert(d.location),
				convertList(d.tags,DtoToMessageConverter::convert), d.keyword, d.name, d.description,
				convert(d.tableHeader), convertList(d.tableBody,DtoToMessageConverter::convert), d.id);
	}

	public static Rule convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Rule d) {
		if (d == null)
			return null;
		return new Rule(convert(d.location),
				convertList(d.tags,DtoToMessageConverter::convert), d.keyword, d.name, d.description,
				convertList(d.children,DtoToMessageConverter::convert), d.id);
	}

	public static RuleChild convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.RuleChild d) {
		if (d == null)
			return null;
		return new RuleChild(DtoToMessageConverter.convert(d.background), convert(d.scenario));
	}

	public static Background convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Background d) {
		if (d == null)
			return null;
		return new Background(convert(d.location), d.keyword, d.name, d.description,
				convertList(d.steps,DtoToMessageConverter::convert), d.id);
	}

	public static Step convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Step d) {
		if (d == null)
			return null;
		return new Step(convert(d.location), d.keyword,
				/* StepKeywordType.valueOf(d.keywordType), */ d.text,null, convert(d.dataTable), d.id);
	}

	public static DocString convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.DocString d) {
		if (d == null)
			return null;
		return new DocString(convert(d.location), d.mediaType, d.content, d.delimiter);
	}

	public static DataTable convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.DataTable d) {
		if (d == null)
			return null;
		return new DataTable(convert(d.location),
				convertList(d.rows,DtoToMessageConverter::convert));
	}

	public static TableRow convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.TableRow d) {
		if (d == null)
			return null;
		return new TableRow(convert(d.location),
				convertList(d.cells,DtoToMessageConverter::convert), d.id);
	}

	public static TableCell convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.TableCell d) {
		if (d == null)
			return null;
		return new TableCell(convert(d.location), d.value);
	}

	public static GherkinDocument convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument d) {
		if (d == null)
			return null;
		return new io.cucumber.messages.types.GherkinDocument(d.uri, convert(d.feature),
				convertList(d.comments,DtoToMessageConverter::convert));
	}

	public static io.cucumber.messages.types.Location convert(io.cucumber.eclipse.java.plugins.dto.Location d) {
		if (d == null)
			return null;
		return new io.cucumber.messages.types.Location(d.line, d.column);
	}

}
