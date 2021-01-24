package io.cucumber.eclipse.editor.document;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import io.cucumber.gherkin.Gherkin;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.Location;
import io.cucumber.messages.IdGenerator;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step.DataTable;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.messages.Messages.ParseError;
import io.cucumber.messages.internal.com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * 
 * Defines unified access from the editor to the parsed content
 * 
 * @author christoph
 *
 */
public final class GherkinEditorDocument {

	private static final List<Function<GherkinDialect, List<String>>> STEP_KEYWORD_KEYS = Arrays.asList(
			GherkinDialect::getGivenKeywords, GherkinDialect::getWhenKeywords, GherkinDialect::getThenKeywords,
			GherkinDialect::getAndKeywords, GherkinDialect::getButKeywords);

	private static final List<Function<GherkinDialect, List<String>>> TOP_LEVEL_KEYWORD = Arrays.asList(
			GherkinDialect::getScenarioKeywords, GherkinDialect::getScenarioOutlineKeywords,
			GherkinDialect::getRuleKeywords, GherkinDialect::getBackgroundKeywords,
			GherkinDialect::getExamplesKeywords);

	private static final ConcurrentHashMap<IDocument, GherkinEditorDocument> DOCUMENT_MAP = new ConcurrentHashMap<>();
	private volatile boolean dirty;
	private final Envelope[] sources;
	// TODO allow definition of default language in preferences
	private final GherkinDialectProvider provider = new GherkinDialectProvider();
	private final GherkinDialect dialect;
	private final IDocument document;
	private final Locale locale;

	private GherkinEditorDocument(IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				GherkinEditorDocument.this.dirty = true;
				document.removeDocumentListener(this);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {

			}
		});
		this.document = document;
		sources = Gherkin.fromSources(Collections.singletonList(Gherkin.makeSourceEnvelope(document.get(), "")), true,
				true, false, new IdGenerator.Incrementing()).toArray(Envelope[]::new);
		System.out.println("Created GherkinDocument with " + sources.length + " envelopes");
		dialect = getFeature().map(f -> f.getLanguage()).filter(Objects::nonNull).filter(Predicate.not(String::isBlank))
				.map(lang -> provider.getDialect(lang, new Location(-1, -1))).orElseGet(provider::getDefaultDialect);
		locale = Locale.forLanguageTag(dialect.getLanguage());
	}

	/**
	 * @return the dialect of the document
	 */
	public GherkinDialect getDialect() {
		return dialect;
	}

	/**
	 * @return the locale of the document as computed by the provided language
	 */
	public Locale getLocale() {
		return locale;
	}

	public IDocument getDocument() {
		return document;
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
		return Arrays.stream(sources).filter(Envelope::hasGherkinDocument).map(s -> s.getGherkinDocument()).findFirst();
	}

	public Stream<FeatureChild> getFeatureChilds() {
		return getFeature().stream().flatMap(feature -> feature.getChildrenList().stream());
	}

	public Stream<Scenario> getScenarios() {
		return getFeatureChilds().filter(FeatureChild::hasScenario).map(FeatureChild::getScenario);
	}

	public Stream<TableRow> getTableHeader() {
		return getScenarios().flatMap(s -> s.getExamplesList().stream()).map(ex -> ex.getTableHeader());
	}

	public Stream<DataTable> getDataTables() {
		return getScenarios().flatMap(scenario -> scenario.getStepsList().stream()).filter(Step::hasDataTable)
				.map(Step::getDataTable);
	}

	/**
	 * @return a stream of parse errors for the given document
	 */
	public Stream<ParseError> getParseError() {
		return Arrays.stream(sources).filter(Envelope::hasParseError).map(Envelope::getParseError);
	}


	/**
	 * @return a stream of all {@link GherkinKeyword}s for the current language that
	 *         are related to steps
	 */
	public Stream<GherkinKeyword> getStepElementKeywords() {
		return STEP_KEYWORD_KEYS.stream().flatMap(this::keyWords);
	}

	/**
	 * @return a stream of all {@link GherkinKeyword}s for the current language that
	 *         are related to steps
	 */
	public Stream<GherkinKeyword> getTopLevelKeywords() {
		return TOP_LEVEL_KEYWORD.stream().flatMap(this::keyWords);
	}

	/**
	 * @return a stream of all {@link GherkinKeyword}s for the current language
	 */
	public Stream<GherkinKeyword> getAllKeywords() {
		return Stream.concat(Stream.concat(getTopLevelKeywords(), getStepElementKeywords()), getFeatureKeywords());
	}

	/**
	 * @return a stream of all {@link GherkinKeyword}s for the current related to
	 *         feature
	 */
	public Stream<GherkinKeyword> getFeatureKeywords() {
		return keyWords(GherkinDialect::getFeatureKeywords);
	}

	/**
	 * 
	 * 
	 * @param keyWords the keywords to fetch
	 * @return A stream of {@link GherkinKeyword}s for the given access function
	 */
	public Stream<GherkinKeyword> keyWords(Function<GherkinDialect, List<String>> keyWords) {
		return keyWords.apply(dialect).stream().map(s -> s.trim())
				.filter(Predicate.not(GherkinEditorDocument::isWildcard))
				.map(str -> new GherkinKeyword(str, locale, dialect));
	}

	/**
	 * A cached instance for the given document, the instance is automatically
	 * updated on each request to {@link GherkinEditorDocument#get(IDocument)}, the
	 * returned instance itself is immutable
	 * 
	 * @param document the document to get the corresponding
	 *                 {@link GherkinEditorDocument} for
	 * @return {@link GherkinEditorDocument} for the given document
	 */
	public static GherkinEditorDocument get(IDocument document) {
		Objects.requireNonNull(document, "document can't be null");
		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
		if (buffer != null) {
			try {
				IContentType contentType = buffer.getContentType();
				if (contentType != null) {
					if ("io.cucumber.eclipse.editor.content-type.feature".equals(contentType.getId())) {
						return DOCUMENT_MAP.compute(document, (key, value) -> {
							if (value == null || value.dirty) {
								System.out.println("Document is null or dirty");
								return parse(key);
							}
							return value;
						});
					}
				}
			} catch (CoreException e) {
			}
		}
		return null;
		
	}

	/**
	 * @return the resource of the document or <code>null</code> if it can't be
	 *         determined
	 */
	public IResource getResource() {
		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
		if (buffer != null) {
			IPath location = buffer.getLocation();
			if (location != null) {
				IFile res = ResourcesPlugin.getWorkspace().getRoot().getFile(location);
				if (res != null && res.exists()) {
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * Parses the string into a (temporary) document
	 * 
	 * @param document the document to parse
	 * @return a detached {@link GherkinEditorDocument} instance
	 */
	public static GherkinEditorDocument parse(IDocument document) {
		return new GherkinEditorDocument(document);
	}

	public static void format(IDocument document) {
		GherkinEditorDocument doc = new GherkinEditorDocument(document);
		for (Envelope env : doc.sources) {
			System.out.println("----------------------");
			Map<FieldDescriptor, Object> allFields = env.getAllFields();
			for (Entry<FieldDescriptor, Object> entry : allFields.entrySet()) {
				System.out.println(entry.getKey().getFullName() + "->" + entry.getValue());
			}
		}
	}

	private static boolean isWildcard(String keyword) {
		return "*".equals(keyword);
	}

}
