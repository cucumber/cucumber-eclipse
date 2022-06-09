package io.cucumber.eclipse.java.plugins.v6;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.cucumber.messages.Messages.Attachment;
import io.cucumber.messages.Messages.Attachment.ContentEncoding;
import io.cucumber.messages.Messages.Duration;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.Envelope.Builder;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Comment;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Background;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.Rule;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step.DataTable;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step.DocString;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow.TableCell;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Tag;
import io.cucumber.messages.Messages.Hook;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.Meta;
import io.cucumber.messages.Messages.Meta.CI;
import io.cucumber.messages.Messages.Meta.CI.Git;
import io.cucumber.messages.Messages.Meta.Product;
import io.cucumber.messages.Messages.ParameterType;
import io.cucumber.messages.Messages.ParseError;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.Pickle.PickleTag;
import io.cucumber.messages.Messages.PickleStepArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable.PickleTableRow;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable.PickleTableRow.PickleTableCell;
import io.cucumber.messages.Messages.Source;
import io.cucumber.messages.Messages.SourceReference;
import io.cucumber.messages.Messages.SourceReference.JavaMethod;
import io.cucumber.messages.Messages.SourceReference.JavaStackTraceElement;
import io.cucumber.messages.Messages.StepDefinition;
import io.cucumber.messages.Messages.StepDefinition.StepDefinitionPattern;
import io.cucumber.messages.Messages.StepDefinition.StepDefinitionPattern.StepDefinitionPatternType;
import io.cucumber.messages.Messages.TestCase;
import io.cucumber.messages.Messages.TestCase.TestStep;
import io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList;
import io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList.StepMatchArgument;
import io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList.StepMatchArgument.Group;
import io.cucumber.messages.Messages.TestCaseFinished;
import io.cucumber.messages.Messages.TestCaseStarted;
import io.cucumber.messages.Messages.TestRunFinished;
import io.cucumber.messages.Messages.TestRunStarted;
import io.cucumber.messages.Messages.TestStepFinished;
import io.cucumber.messages.Messages.TestStepFinished.TestStepResult;
import io.cucumber.messages.Messages.TestStepFinished.TestStepResult.Status;
import io.cucumber.messages.Messages.TestStepStarted;
import io.cucumber.messages.Messages.Timestamp;
import io.cucumber.messages.Messages.UndefinedParameterType;

public class DtoToMessageConverter {

	public static Envelope convert(io.cucumber.eclipse.java.plugins.dto.Envelope d) {
		if (d == null)
			return null;
		Builder builder = Envelope.newBuilder();
		if (d.attachment != null)
			builder.setAttachment(convert(d.attachment));
		if (d.gherkinDocument != null)
			builder.setGherkinDocument(convert(d.gherkinDocument));
		if (d.hook != null)
			builder.setHook(convert(d.hook));
		if (d.meta != null)
			builder.setMeta(convert(d.meta));
		if (d.parameterType != null)
			builder.setParameterType(convert(d.parameterType));
		if (d.parseError != null)
			builder.setParseError(convert(d.parseError));
		if (d.pickle != null)
			builder.setPickle(convert(d.pickle));
		if (d.source != null)
			builder.setSource(convert(d.source));
		if (d.stepDefinition != null)
			builder.setStepDefinition(convert(d.stepDefinition));
		if (d.testCase != null)
			builder.setTestCase(convert(d.testCase));
		if (d.testCaseFinished != null)
			builder.setTestCaseFinished(convert(d.testCaseFinished));
		if (d.testCaseStarted != null)
			builder.setTestCaseStarted(convert(d.testCaseStarted));
		if (d.testRunFinished != null)
			builder.setTestRunFinished(convert(d.testRunFinished));
		if (d.testRunStarted != null)
			builder.setTestRunStarted(convert(d.testRunStarted));
		if (d.testStepFinished != null)
			builder.setTestStepFinished(convert(d.testStepFinished));
		if (d.testStepStarted != null)
			builder.setTestStepStarted(convert(d.testStepStarted));
		if (d.undefinedParameterType != null)
			builder.setUndefinedParameterType(convert(d.undefinedParameterType));

		return builder.build();
	}

	public static <T, K> List<K> convertList(List<T> list, Function<T, K> converter) {
		Optional<List<K>> map = Optional.ofNullable(list)
				.map(l -> l.stream().filter(o -> o != null).map(v -> converter.apply(v)).collect(toList()));
		return map.orElse(null);
	}

	public static UndefinedParameterType convert(io.cucumber.eclipse.java.plugins.dto.UndefinedParameterType d) {
		if (d == null)
			return null;
		return UndefinedParameterType.newBuilder().setExpression(d.expression).setName(d.name).build();
	}

	public static TestStepStarted convert(io.cucumber.eclipse.java.plugins.dto.TestStepStarted d) {
		if (d == null)
			return null;
		var builder = TestStepStarted.newBuilder().setTestCaseStartedId(d.testCaseStartedId)
				.setTestStepId(d.testStepId);
		if (d.timestamp != null)
			builder.setTimestamp(convert(d.timestamp));
		return builder.build();

	}

	public static Timestamp convert(io.cucumber.eclipse.java.plugins.dto.Timestamp d) {
		if (d == null)
			return null;
		return Timestamp.newBuilder().setSeconds(d.seconds).setNanos(d.nanos.intValue()).build();
	}

	public static TestStepFinished convert(io.cucumber.eclipse.java.plugins.dto.TestStepFinished d) {
		if (d == null)
			return null;
		var builder = TestStepFinished.newBuilder().setTestCaseStartedId(d.testCaseStartedId)
				.setTestStepId(d.testStepId).setTimestamp(convert(d.timestamp));
		if (d.testStepResult != null)
			builder.setTestStepResult(convert(d.testStepResult));

		return builder.build();
	}

	public static TestStepResult convert(io.cucumber.eclipse.java.plugins.dto.TestStepFinished.TestStepResult d) {
		if (d == null)
			return null;
		var builder = TestStepResult.newBuilder().setStatus(Status.valueOf(d.status));
		if (d.duration != null)
			builder.setDuration(convert(d.duration));
		if (d.message != null)
			builder.setMessage(d.message);
		return builder.build();
	}

	public static Duration convert(io.cucumber.eclipse.java.plugins.dto.Duration d) {
		if (d == null)
			return null;
		return Duration.newBuilder().setSeconds(d.seconds).setNanos(d.nanos.intValue()).build();
	}

	public static TestRunStarted convert(io.cucumber.eclipse.java.plugins.dto.TestRunStarted d) {
		if (d == null)
			return null;
		var builder = TestRunStarted.newBuilder();
		if (d.timestamp != null)
			builder.setTimestamp(convert(d.timestamp));
		return builder.build();
	}

	public static TestRunFinished convert(io.cucumber.eclipse.java.plugins.dto.TestRunFinished d) {
		if (d == null)
			return null;
		var builder = TestRunFinished.newBuilder().setSuccess(d.success);
		if (d.message != null)
			builder.setMessage(d.message);
		if (d.timestamp != null)
			builder.setTimestamp(convert(d.timestamp));
		return builder.build();
	}

	public static TestCaseStarted convert(io.cucumber.eclipse.java.plugins.dto.TestCaseStarted d) {
		if (d == null)
			return null;
		var builder = TestCaseStarted.newBuilder().setAttempt(d.attempt.intValue()).setId(d.id)
				.setTestCaseId(d.testCaseId);
		if (d.timestamp != null)
			builder.setTimestamp(convert(d.timestamp));
		return builder.build();
	}

	public static TestCaseFinished convert(io.cucumber.eclipse.java.plugins.dto.TestCaseFinished d) {
		if (d == null)
			return null;
		var builder = TestCaseFinished.newBuilder().setTestCaseStartedId(d.testCaseStartedId);
		if (d.timestamp != null)
			builder.setTimestamp(convert(d.timestamp));
		return builder.build();
	}

	public static TestCase convert(io.cucumber.eclipse.java.plugins.dto.TestCase d) {
		if (d == null)
			return null;
		return TestCase.newBuilder().setId(d.id).setPickleId(d.pickleId)
				.addAllTestSteps(convertList(d.testSteps, DtoToMessageConverter::convert)).build();
	}

	public static TestStep convert(io.cucumber.eclipse.java.plugins.dto.TestCase.TestStep d) {
		if (d == null)
			return null;
		io.cucumber.messages.Messages.TestCase.TestStep.Builder builder = TestStep.newBuilder().setId(d.id)
				.setPickleStepId(d.id);
		if (d.hookId != null)
			builder.setHookId(d.hookId);
		if (d.stepDefinitionIds != null)
			builder.addAllStepDefinitionIds(d.stepDefinitionIds);
		if (d.stepMatchArgumentsLists != null)
			builder.addAllStepMatchArgumentsLists(
					convertList(d.stepMatchArgumentsLists, DtoToMessageConverter::convert));
		return builder.build();
	}

	public static StepMatchArgumentsList convert(
			io.cucumber.eclipse.java.plugins.dto.TestCase.StepMatchArgumentsList d) {
		if (d == null)
			return null;
		return StepMatchArgumentsList.newBuilder()
				.addAllStepMatchArguments(convertList(d.stepMatchArguments, DtoToMessageConverter::convert)).build();
	}

	public static StepMatchArgument convert(io.cucumber.eclipse.java.plugins.dto.TestCase.StepMatchArgument d) {
		if (d == null)
			return null;
		var builder = StepMatchArgument.newBuilder().setParameterTypeName(d.parameterTypeName);
		if (d.group != null)
			builder.setGroup(convert(d.group));
		return builder.build();
	}

	public static Group convert(io.cucumber.eclipse.java.plugins.dto.TestCase.Group d) {
		if (d == null)
			return null;
		io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList.StepMatchArgument.Group.Builder builder = Group
				.newBuilder().addAllChildren(convertList(d.children, DtoToMessageConverter::convert))
				.setStart(d.start.intValue());
		if (d.value != null)
			builder.setValue(d.value);
		return builder.build();
	}

	public static StepDefinition convert(io.cucumber.eclipse.java.plugins.dto.StepDefinition d) {
		if (d == null)
			return null;
		var builder = StepDefinition.newBuilder().setId(d.id);
		if (d.pattern != null)
			builder.setPattern(convert(d.pattern));
		if (d.sourceReference != null)
			builder.setSourceReference(convert(d.sourceReference));
		return builder.build();
	}

	public static StepDefinitionPattern convert(
			io.cucumber.eclipse.java.plugins.dto.StepDefinition.StepDefinitionPattern d) {
		if (d == null)
			return null;
		return StepDefinitionPattern.newBuilder().setSource(d.source).setType(StepDefinitionPatternType.valueOf(d.type))
				.build();
	}

	public static Source convert(io.cucumber.eclipse.java.plugins.dto.Source d) {
		if (d == null)
			return null;
		return Source.newBuilder().setUri(d.uri).setData(d.data).setMediaType(d.mediaType).build();
	}

	public static SourceReference convert1(io.cucumber.eclipse.java.plugins.dto.Source d) {
		if (d == null)
			return null;
		return SourceReference.newBuilder().setUri(d.uri).build();
	}

	public static Pickle convert(io.cucumber.eclipse.java.plugins.dto.Pickle d) {
		if (d == null)
			return null;

		return Pickle.newBuilder().setId(d.id).setUri(d.uri).setName(d.name).setLanguage(d.language)
				.addAllSteps(convertList(d.steps, DtoToMessageConverter::convert))
				.addAllTags(convertList(d.tags, DtoToMessageConverter::convert)).addAllAstNodeIds(d.astNodeIds).build();
	}

	public static PickleTag convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTag d) {
		if (d == null)
			return null;
		return PickleTag.newBuilder().setName(d.name).setAstNodeId(d.astNodeId).build();
	}

	public static PickleStep convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleStep d) {
		if (d == null)
			return null;
		var builder = PickleStep.newBuilder().addAllAstNodeIds(d.astNodeIds).setId(d.id);
		if (d.text != null)
			builder.setText(d.text);
		if (d.argument != null)
			builder.setArgument(convert(d.argument));
		return builder.build();
	}

	public static PickleStepArgument convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleStepArgument d) {
		if (d == null)
			return null;
		var builder = PickleStepArgument.newBuilder();
		if (d.docString != null)
			builder.setDocString(convert(d.docString));
		if (d.dataTable != null)
			builder.setDataTable(convert(d.dataTable));
		return builder.build();
	}

	public static PickleTable convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTable d) {
		if (d == null)
			return null;
		return PickleTable.newBuilder().addAllRows(convertList(d.rows, DtoToMessageConverter::convert)).build();
	}

	public static PickleTableRow convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTableRow d) {
		if (d == null)
			return null;
		return PickleTableRow.newBuilder().addAllCells(convertList(d.cells, DtoToMessageConverter::convert)).build();
	}

	public static PickleTableCell convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleTableCell d) {
		if (d == null)
			return null;
		return PickleTableCell.newBuilder().setValue(d.value).build();
	}

	public static PickleDocString convert(io.cucumber.eclipse.java.plugins.dto.Pickle.PickleDocString d) {
		if (d == null)
			return null;
		return PickleDocString.newBuilder().setMediaType(d.mediaType).setContent(d.content).build();
	}

	public static ParseError convert(io.cucumber.eclipse.java.plugins.dto.ParseError d) {
		if (d == null)
			return null;
		var builder = ParseError.newBuilder().setMessage(d.message);
		if (d.source != null)
			builder.setSource(convert(d.source));
		return builder.build();
	}

	public static ParameterType convert(io.cucumber.eclipse.java.plugins.dto.ParameterType d) {
		if (d == null)
			return null;
		return ParameterType.newBuilder().setName(d.name).addAllRegularExpressions(d.regularExpressions)
				.setPreferForRegularExpressionMatch(d.preferForRegularExpressionMatch)
				.setUseForSnippets(d.useForSnippets).build();
	}

	public static Meta convert(io.cucumber.eclipse.java.plugins.dto.Meta d) {
		if (d == null)
			return null;
		var builder = Meta.newBuilder().setProtocolVersion(d.protocolVersion);

		if (d.implementation != null)
			builder.setImplementation(convert(d.implementation));
		if (d.runtime != null)
			builder.setRuntime(convert(d.runtime));
		if (d.os != null)
			builder.setOs(convert(d.os));
		if (d.cpu != null)
			builder.setCpu(convert(d.cpu));
		if (d.ci != null)
			builder.setCi(convert(d.ci));
		return builder.build();
	}

	public static CI convert(io.cucumber.eclipse.java.plugins.dto.Meta.Ci d) {
		if (d == null)
			return null;
		var builder = CI.newBuilder().setName(d.name).setUrl(d.url);
		if (d.git != null)
			builder.setGit(convert(d.git));
		return builder.build();
	}

	public static Git convert(io.cucumber.eclipse.java.plugins.dto.Meta.Git d) {
		if (d == null)
			return null;
//		return new Git(d.remote, d.revision, d.branch, d.tag);
		return Git.newBuilder().setRemote(d.remote).setRevision(d.revision).setBranch(d.branch).setTag(d.tag).build();
	}

	public static Product convert(io.cucumber.eclipse.java.plugins.dto.Meta.Product d) {
		if (d == null)
			return null;
		io.cucumber.messages.Messages.Meta.Product.Builder setName = Product.newBuilder().setName(d.name);
		if (d.version != null)
			setName.setVersion(d.version);
		return setName.build();
	}

	public static Hook convert(io.cucumber.eclipse.java.plugins.dto.Hook d) {
		if (d == null)
			return null;
		var builder = Hook.newBuilder().setId(d.id).setTagExpression(d.tagExpression);
		if (d.sourceReference != null)
			builder.setSourceReference(convert(d.sourceReference));
		return builder.build();
	}

	public static SourceReference convert(io.cucumber.eclipse.java.plugins.dto.SourceReference d) {
		if (d == null)
			return null;
		var builder = SourceReference.newBuilder();
		if (d.uri != null)
			builder.setUri(d.uri);
		if (d.javaMethod != null)
			builder.setJavaMethod(convert(d.javaMethod));
		if (d.javaStackTraceElement != null)
			builder.setJavaStackTraceElement(convert(d.javaStackTraceElement));
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static JavaMethod convert(io.cucumber.eclipse.java.plugins.dto.SourceReference.JavaMethod d) {
		if (d == null)
			return null;
		return JavaMethod.newBuilder().setClassName(d.className).setMethodName(d.methodName)
				.addAllMethodParameterTypes(d.methodParameterTypes).build();
	}

	public static JavaStackTraceElement convert(
			io.cucumber.eclipse.java.plugins.dto.SourceReference.JavaStackTraceElement d) {
		if (d == null)
			return null;
		return JavaStackTraceElement.newBuilder().setClassName(d.className).setFileName(d.fileName)
				.setMethodName(d.methodName).build();
	}

	public static Attachment convert(io.cucumber.eclipse.java.plugins.dto.Attachment d) {
		if (d == null)
			return null;
		var builder = Attachment.newBuilder().setBody(d.body)
				.setContentEncoding(ContentEncoding.valueOf(d.contentEncoding)).setFileName(d.fileName)
				.setMediaType(d.mediaType);
		if (d.source != null)
			builder.setSource(convert1(d.source));
		return builder.build();
	}

	public static Comment convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Comment d) {
		if (d == null)
			return null;
//		return new Comment(convert(d.location), d.text);
		return Comment.newBuilder().setLocation(convert(d.location)).setText(d.text).build();
	}

	public static Feature convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Feature d) {
		if (d == null)
			return null;

		var builder = Feature.newBuilder().setLanguage(d.language).setKeyword(d.keyword).setName(d.name)
				.setDescription(d.description);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static Tag convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Tag d) {
		if (d == null)
			return null;
		var builder = Tag.newBuilder().setName(d.name).setId(d.id);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static FeatureChild convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.FeatureChild d) {
		if (d == null)
			return null;
		var builder = FeatureChild.newBuilder();
		if (d.rule != null)
			builder.setRule(convert(d.rule));
		if (d.background != null)
			builder.setBackground(convert(d.background));
		if (d.scenario != null)
			builder.setScenario(convert(d.scenario));
		return builder.build();
	}

	public static Scenario convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Scenario d) {
		if (d == null)
			return null;
		var builder = Scenario.newBuilder().addAllTags(convertList(d.tags, DtoToMessageConverter::convert))
				.setKeyword(d.keyword).setName(d.name).setDescription(d.description)
				.addAllSteps(convertList(d.steps, DtoToMessageConverter::convert))
				.addAllExamples(convertList(d.examples, DtoToMessageConverter::convert)).setId(d.id);

		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static Examples convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Examples d) {
		if (d == null)
			return null;

		var builder = Examples.newBuilder().addAllTags(convertList(d.tags, DtoToMessageConverter::convert))
				.setKeyword(d.keyword).setName(d.name).setDescription(d.description)
				.setTableHeader(convert(d.tableHeader))
				.addAllTableBody(convertList(d.tableBody, DtoToMessageConverter::convert)).setId(d.id);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static Rule convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Rule d) {
		if (d == null)
			return null;
		var builder = Rule.newBuilder().setKeyword(d.keyword).setName(d.name).setDescription(d.description)
				.addAllChildren(convertList(d.children, DtoToMessageConverter::convert)).setId(d.id);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static RuleChild convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.RuleChild d) {
		if (d == null)
			return null;
		var builder = RuleChild.newBuilder();

		if (d.background != null)
			builder.setBackground(convert(d.background));
		if (d.scenario != null)
			builder.setScenario(convert(d.scenario));
		return builder.build();

	}

	public static Background convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Background d) {
		if (d == null)
			return null;
		var builder = Background.newBuilder().setKeyword(d.keyword).setName(d.name).setDescription(d.description)
				.addAllSteps(convertList(d.steps, DtoToMessageConverter::convert));
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static Step convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.Step d) {
		if (d == null)
			return null;
		var builder = Step.newBuilder().setKeyword(d.keyword).setText(d.text).setId(d.id);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		if (d.docString != null)
			builder.setDocString(convert(d.docString));
		if (d.dataTable != null)
			builder.setDataTable(convert(d.dataTable));
		return builder.build();
	}

	public static DocString convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.DocString d) {
		if (d == null)
			return null;
		var builder = DocString.newBuilder().setMediaType(d.mediaType).setContent(d.content).setDelimiter(d.delimiter);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static DataTable convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.DataTable d) {
		if (d == null)
			return null;
		var builder = DataTable.newBuilder().addAllRows(convertList(d.rows, DtoToMessageConverter::convert));
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static TableRow convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.TableRow d) {
		if (d == null)
			return null;
		var builder = TableRow.newBuilder().addAllCells(convertList(d.cells, DtoToMessageConverter::convert));
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static TableCell convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument.TableCell d) {
		if (d == null)
			return null;
		var builder = TableCell.newBuilder().setValue(d.value);
		if (d.location != null)
			builder.setLocation(convert(d.location));
		return builder.build();
	}

	public static GherkinDocument convert(io.cucumber.eclipse.java.plugins.dto.GherkinDocument d) {
		if (d == null)
			return null;
		var builder = GherkinDocument.newBuilder().setUri(d.uri)
				.addAllComments(convertList(d.comments, DtoToMessageConverter::convert));
		if (d.feature != null)
			builder.setFeature(convert(d.feature));
		return builder.build();
	}

	public static Location convert(io.cucumber.eclipse.java.plugins.dto.Location d) {
		if (d == null)
			return null;
		return Location.newBuilder().setLine(toInt(d.line)).setColumn(toInt(d.column)).build();
	}

	private static int toInt(Long l) {
		return Optional.ofNullable(l).map(v -> v.intValue()).orElse(-1);
	}

}
