package io.cucumber.eclipse.java.plugins.v7;

import static java.util.stream.Collectors.toList;

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

	public static Envelope convert(io.cucumber.messages.types.Envelope d) {
		if (d == null)
			return null;
		return new Envelope(
				d.getAttachment().map(MessageToDtoConverter::convert).orElse(null),
				d.getGherkinDocument().map(MessageToDtoConverter::convert).orElse(null),
				d.getHook().map(MessageToDtoConverter::convert).orElse(null),
				d.getMeta().map(MessageToDtoConverter::convert).orElse(null),
				d.getParameterType().map(MessageToDtoConverter::convert).orElse(null),
				d.getParseError().map(MessageToDtoConverter::convert).orElse(null),
				d.getPickle().map(MessageToDtoConverter::convert).orElse(null),
				d.getSource().map(MessageToDtoConverter::convert).orElse(null),
				d.getStepDefinition().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestCase().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestCaseFinished().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestCaseStarted().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestRunFinished().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestRunStarted().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestStepFinished().map(MessageToDtoConverter::convert).orElse(null),
				d.getTestStepStarted().map(MessageToDtoConverter::convert).orElse(null),
				d.getUndefinedParameterType().map(MessageToDtoConverter::convert).orElse(null));
	}

	public static <T, K> List<K> convertList(List<T> list, Function<T, K> converter) {
		Optional<List<K>> map = Optional.ofNullable(list)
				.map(l -> l.stream().map(v -> converter.apply(v)).collect(toList()));
		return map.orElse(null);
	}

	public static <T, K> List<K> convertList(Optional<List<T>> list, Function<T, K> converter) {
		Optional<List<K>> map = list.map(l -> l.stream().map(v -> converter.apply(v)).collect(toList()));
		return map.orElse(null);
	}

	private static UndefinedParameterType convert(io.cucumber.messages.types.UndefinedParameterType d) {
		if (d == null)
			return null;
		return new UndefinedParameterType(d.getExpression(), d.getName());
	}

	public static TestStepStarted convert(io.cucumber.messages.types.TestStepStarted d) {
		if (d == null)
			return null;
		return new TestStepStarted(d.getTestCaseStartedId(), d.getTestStepId(), convert(d.getTimestamp()));
	}

	public static Timestamp convert(io.cucumber.messages.types.Timestamp d) {
		if (d == null)
			return null;
		return new Timestamp(d.getSeconds(), d.getNanos());
	}

	public static TestStepFinished convert(io.cucumber.messages.types.TestStepFinished d) {
		if (d == null)
			return null;
		return new TestStepFinished(d.getTestCaseStartedId(), d.getTestStepId(), convert(d.getTestStepResult()),
				convert(d.getTimestamp()));
	}

	public static TestStepResult convert(io.cucumber.messages.types.TestStepResult d) {
		if (d == null)
			return null;
		return new TestStepResult(convert(d.getDuration()), d.getMessage().orElse(null), d.getStatus().value());
	}

	public static Duration convert(io.cucumber.messages.types.Duration d) {
		if (d == null)
			return null;
		return new Duration(d.getSeconds(), d.getNanos());
	}

	public static TestRunStarted convert(io.cucumber.messages.types.TestRunStarted d) {
		if (d == null)
			return null;
		return new TestRunStarted(convert(d.getTimestamp()));
	}

	public static TestRunFinished convert(io.cucumber.messages.types.TestRunFinished d) {
		if (d == null)
			return null;
		return new TestRunFinished(d.getMessage().orElse(null), d.getSuccess(), convert(d.getTimestamp()));
	}

	public static TestCaseStarted convert(io.cucumber.messages.types.TestCaseStarted d) {
		if (d == null)
			return null;
		return new TestCaseStarted(d.getAttempt(), d.getId(), d.getTestCaseId(), convert(d.getTimestamp()));
	}

	public static TestCaseFinished convert(io.cucumber.messages.types.TestCaseFinished d) {
		if (d == null)
			return null;
		return new TestCaseFinished(d.getTestCaseStartedId(), convert(d.getTimestamp()), d.getWillBeRetried());
	}

	public static TestCase convert(io.cucumber.messages.types.TestCase d) {
		if (d == null)
			return null;
		return new TestCase(d.getId(), d.getPickleId(), convertList(d.getTestSteps(), MessageToDtoConverter::convert));
	}

	public static TestStep convert(io.cucumber.messages.types.TestStep d) {
		if (d == null)
			return null;
		return new TestStep(d.getHookId().orElse(null), d.getId(), d.getPickleStepId().orElse(null),
				d.getStepDefinitionIds().orElse(null),
				convertList(d.getStepMatchArgumentsLists(), MessageToDtoConverter::convert));
	}

	public static StepMatchArgumentsList convert(io.cucumber.messages.types.StepMatchArgumentsList d) {
		if (d == null)
			return null;
		return new StepMatchArgumentsList(convertList(d.getStepMatchArguments(), MessageToDtoConverter::convert));
	}

	public static StepMatchArgument convert(io.cucumber.messages.types.StepMatchArgument d) {
		if (d == null)
			return null;
		return new StepMatchArgument(convert(d.getGroup()), d.getParameterTypeName().orElse(null));
	}

	public static Group convert(io.cucumber.messages.types.Group d) {
		if (d == null)
			return null;
		return new Group(convertList(d.getChildren(), MessageToDtoConverter::convert), d.getStart().orElse(null),
				d.getValue().orElse(null));
	}

	public static StepDefinition convert(io.cucumber.messages.types.StepDefinition d) {
		if (d == null)
			return null;
		return new StepDefinition(d.getId(), convert(d.getPattern()), convert(d.getSourceReference()));
	}

	public static StepDefinitionPattern convert(io.cucumber.messages.types.StepDefinitionPattern d) {
		if (d == null)
			return null;
		return new StepDefinitionPattern(d.getSource(), d.getType().value());
	}

	public static Source convert(io.cucumber.messages.types.Source d) {
		if (d == null)
			return null;
		return new Source(d.getUri(), d.getData(), d.getMediaType().value());
	}

	public static Pickle convert(io.cucumber.messages.types.Pickle d) {
		if (d == null)
			return null;
		return new Pickle(d.getId(), d.getUri(), d.getName(), d.getLanguage(), convertList(d.getSteps(), MessageToDtoConverter::convert),
				convertList(d.getTags(), MessageToDtoConverter::convert), d.getAstNodeIds());
	}

	public static PickleTag convert(io.cucumber.messages.types.PickleTag d) {
		if (d == null)
			return null;
		return new PickleTag(d.getName(), d.getAstNodeId());
	}

	public static PickleStep convert(io.cucumber.messages.types.PickleStep d) {
		if (d == null)
			return null;
		return new PickleStep(convert(d.getArgument().orElse(null)), d.getAstNodeIds(), d.getId(), d.getText(),/*d.getType().map(PickleStepType::value).orElse(null)*/null);
	}

	public static PickleStepArgument convert(io.cucumber.messages.types.PickleStepArgument d) {
		if (d == null)
			return null;
		return new PickleStepArgument(convert(d.getDocString().orElse(null)), convert(d.getDataTable().orElse(null)));
	}

	public static PickleTable convert(io.cucumber.messages.types.PickleTable d) {
		if (d == null)
			return null;
		return new PickleTable(convertList(d.getRows(), MessageToDtoConverter::convert));
	}

	public static PickleTableRow convert(io.cucumber.messages.types.PickleTableRow d) {
		if (d == null)
			return null;
		return new PickleTableRow(convertList(d.getCells(), MessageToDtoConverter::convert));
	}

	public static PickleTableCell convert(io.cucumber.messages.types.PickleTableCell d) {
		if (d == null)
			return null;
		return new PickleTableCell(d.getValue());
	}

	public static PickleDocString convert(io.cucumber.messages.types.PickleDocString d) {
		if (d == null)
			return null;
		return new PickleDocString(d.getMediaType().orElse(null), d.getContent());
	}

	public static ParseError convert(io.cucumber.messages.types.ParseError d) {
		if (d == null)
			return null;
		return new ParseError(convert(d.getSource()), d.getMessage());
	}

	public static ParameterType convert(io.cucumber.messages.types.ParameterType d) {
		if (d == null)
			return null;
		return new ParameterType(d.getName(), d.getRegularExpressions(), d.getPreferForRegularExpressionMatch(), d.getUseForSnippets(),
				d.getId());
	}

	public static Meta convert(io.cucumber.messages.types.Meta d) {
		if (d == null)
			return null;
		return new Meta(d.getProtocolVersion(), convert(d.getImplementation()), convert(d.getRuntime()), convert(d.getOs()), convert(d.getCpu()),
				convert(d.getCi().orElse(null)));
	}

	public static Ci convert(io.cucumber.messages.types.Ci d) {
		if (d == null)
			return null;
		return new Ci(d.getName(), d.getUrl().orElse(null), d.getBuildNumber().orElse(null), convert(d.getGit().orElse(null)));
	}

	public static Git convert(io.cucumber.messages.types.Git d) {
		if (d == null)
			return null;
		return new Git(d.getRemote(), d.getRevision(), d.getBranch().orElse(null), d.getTag().orElse(null));
	}

	public static Product convert(io.cucumber.messages.types.Product d) {
		if (d == null)
			return null;
		return new Product(d.getName(), d.getVersion().orElse(null));
	}

	public static Hook convert(io.cucumber.messages.types.Hook d) {
		if (d == null)
			return null;
		return new Hook(d.getId(), d.getName().orElse(null), convert(d.getSourceReference()), d.getTagExpression().orElse(null));
	}

	public static SourceReference convert(io.cucumber.messages.types.SourceReference d) {
		if (d == null)
			return null;
		return new SourceReference(d.getUri().orElse(null), convert(d.getJavaMethod().orElse(null)), convert(d.getJavaStackTraceElement().orElse(null)), convert(d.getLocation().orElse(null)));
	}

	public static JavaMethod convert(io.cucumber.messages.types.JavaMethod d) {
		if (d == null)
			return null;
		return new JavaMethod(d.getClassName(), d.getMethodName(), d.getMethodParameterTypes());
	}

	public static JavaStackTraceElement convert(io.cucumber.messages.types.JavaStackTraceElement d) {
		if (d == null)
			return null;
		return new JavaStackTraceElement(d.getClassName(), d.getFileName(), d.getMethodName());
	}

	public static Attachment convert(io.cucumber.messages.types.Attachment d) {
		if (d == null)
			return null;
		return new Attachment(d.getBody(), d.getContentEncoding().value(), d.getFileName().orElse(null), d.getMediaType(), convert(d.getSource().orElse(null)),
				d.getTestCaseStartedId().orElse(null), d.getTestStepId().orElse(null), d.getUrl().orElse(null));
	}

	public static Comment convert(io.cucumber.messages.types.Comment d) {
		if (d == null)
			return null;
		return new Comment(convert(d.getLocation()), d.getText());
	}

	public static Feature convert(io.cucumber.messages.types.Feature d) {
		if (d == null)
			return null;
		return new Feature(convert(d.getLocation()),
				convertList(d.getTags(), MessageToDtoConverter::convert), d.getLanguage(), d.getKeyword(), d.getName(), d.getDescription(),
				convertList(d.getChildren(), MessageToDtoConverter::convert));
	}

	public static Tag convert(io.cucumber.messages.types.Tag d) {
		if (d == null)
			return null;
		return new Tag(convert(d.getLocation()), d.getName(), d.getId());
	}

	public static FeatureChild convert(io.cucumber.messages.types.FeatureChild d) {
		if (d == null)
			return null;
		return new FeatureChild(convert(d.getRule().orElse(null)), convert(d.getBackground().orElse(null)),
				convert(d.getScenario().orElse(null)));
	}

	public static Scenario convert(io.cucumber.messages.types.Scenario d) {
		if (d == null)
			return null;
		return new Scenario(convert(d.getLocation()),
				convertList(d.getTags(), MessageToDtoConverter::convert), d.getKeyword(), d.getName(), d.getDescription(),
				convertList(d.getSteps(), MessageToDtoConverter::convert),
				convertList(d.getExamples(), MessageToDtoConverter::convert), d.getId());
	}

	public static Examples convert(io.cucumber.messages.types.Examples d) {
		if (d == null)
			return null;
		return new Examples(convert(d.getLocation()),
				convertList(d.getTags(), MessageToDtoConverter::convert), d.getKeyword(), d.getName(), d.getDescription(),
				convert(d.getTableHeader().orElse(null)), convertList(d.getTableBody(), MessageToDtoConverter::convert), d.getId());
	}

	public static Rule convert(io.cucumber.messages.types.Rule d) {
		if (d == null)
			return null;
		return new Rule(convert(d.getLocation()),
				convertList(d.getTags(), MessageToDtoConverter::convert), d.getKeyword(), d.getName(), d.getDescription(),
				convertList(d.getChildren(), MessageToDtoConverter::convert), d.getId());
	}

	public static RuleChild convert(io.cucumber.messages.types.RuleChild d) {
		if (d == null)
			return null;
		return new RuleChild(MessageToDtoConverter.convert(d.getBackground().orElse(null)),
				convert(d.getScenario().orElse(null)));
	}

	public static Background convert(io.cucumber.messages.types.Background d) {
		if (d == null)
			return null;
		return new Background(convert(d.getLocation()), d.getKeyword(), d.getName(), d.getDescription(),
				convertList(d.getSteps(), MessageToDtoConverter::convert), d.getId());
	}

	public static Step convert(io.cucumber.messages.types.Step d) {
		if (d == null)
			return null;
		return new Step(convert(d.getLocation()), d.getKeyword(),/*d.getKeywordType().map(t->t.value()).orElse(null)*/null, d.getText(), convert(d.getDocString().orElse(null)),
				convert(d.getDataTable().orElse(null)), d.getId());
	}

	public static DocString convert(io.cucumber.messages.types.DocString d) {
		if (d == null)
			return null;
		return new DocString(convert(d.getLocation()), d.getMediaType().orElse(null), d.getContent(), d.getDelimiter());
	}

	public static DataTable convert(io.cucumber.messages.types.DataTable d) {
		if (d == null)
			return null;
		return new DataTable(convert(d.getLocation()),
				convertList(d.getRows(), MessageToDtoConverter::convert));
	}

	public static TableRow convert(io.cucumber.messages.types.TableRow d) {
		if (d == null)
			return null;
		return new TableRow(convert(d.getLocation()),
				convertList(d.getCells(), MessageToDtoConverter::convert), d.getId());
	}

	public static TableCell convert(io.cucumber.messages.types.TableCell d) {
		if (d == null)
			return null;
		return new TableCell(convert(d.getLocation()), d.getValue());
	}

	public static GherkinDocument convert(io.cucumber.messages.types.GherkinDocument d) {
		if (d == null)
			return null;
		return new GherkinDocument(d.getUri().orElse(null), convert(d.getFeature().orElse(null)),
				convertList(d.getComments(), MessageToDtoConverter::convert));
	}

	public static Location convert(io.cucumber.messages.types.Location d) {
		if (d == null)
			return null;
		return new Location(d.getLine(), d.getColumn().orElse(null));
	}
}
