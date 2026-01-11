package io.cucumber.eclipse.editor.document;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.SourceMediaType;

/**
 * Provides unified access to Gherkin feature file content from the Eclipse editor.
 * <p>
 * This class extends {@link GherkinStream} to parse and expose Gherkin documents,
 * including features, scenarios, steps, and language-specific keywords.
 * </p>
 * <p>
 * Key features:
 * <ul>
 * <li>Parses Gherkin feature files using the Cucumber Gherkin parser</li>
 * <li>Provides access to language-specific keywords and dialects</li>
 * <li>Maintains position mapping between Gherkin elements and document locations</li>
 * <li>Tracks dirty state when underlying document changes</li>
 * </ul>
 * </p>
 * <p>
 * Use {@link GherkinEditorDocumentManager} to obtain cached instances.
 * </p>
 * 
 * @author christoph
 */
public final class GherkinEditorDocument extends GherkinStream {

	private static final List<Function<GherkinDialect, List<String>>> STEP_KEYWORD_KEYS = Arrays.asList(
			GherkinDialect::getGivenKeywords, GherkinDialect::getWhenKeywords, GherkinDialect::getThenKeywords,
			GherkinDialect::getAndKeywords, GherkinDialect::getButKeywords);

	private static final List<Function<GherkinDialect, List<String>>> TOP_LEVEL_KEYWORD = Arrays.asList(
			GherkinDialect::getScenarioKeywords, GherkinDialect::getScenarioOutlineKeywords,
			GherkinDialect::getRuleKeywords, GherkinDialect::getBackgroundKeywords,
			GherkinDialect::getExamplesKeywords);

	// TODO allow definition of default language in preferences
	private final GherkinDialectProvider provider = new GherkinDialectProvider();

	private volatile boolean dirty;
	private final IDocument document;
	private final GherkinDialect dialect;
	private final Locale locale;

	private Supplier<IResource> resourceSupplier;

	GherkinEditorDocument(IDocument document, Supplier<IResource> resourceSupplier) {

		super(getEnvelopes(document));
		this.resourceSupplier = resourceSupplier;
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

		Optional<String> langOpt = getFeature().map(f -> f.getLanguage()).filter(Objects::nonNull)
				.filter(Predicate.not(String::isBlank));

		dialect = langOpt.flatMap(lang -> provider.getDialect(lang)).or(() -> {
			try {
				IRegion firstLine = document.getLineInformation(0);
				String line = document.get(firstLine.getOffset(), firstLine.getLength()).trim();
				if (line.startsWith("#")) {
					String[] split = line.split("language:", 2);
					if (split.length == 2) {
						try {
							return provider.getDialect(split[1].trim());
						} catch (Exception e) {
						}
					}

				}
			} catch (BadLocationException e) {
			}
			return Optional.of(provider.getDefaultDialect());
		}).get();

		locale = Locale.forLanguageTag(dialect.getLanguage());

	}

	boolean isDirty() {
		return dirty;
	}

	private static Envelope[] getEnvelopes(IDocument document) {
		GherkinParser parser = GherkinParser.builder()
				.includeSource(true)
				.includeGherkinDocument(true)
				.includePickles(false)
				.build();
		Source source = new Source("", document.get(), SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN);
		Envelope envelope = Envelope.of(source);
		return parser.parse(envelope).toArray(Envelope[]::new);
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

	/**
	 * @return the underlying Eclipse document
	 */
	public IDocument getDocument() {
		return document;
	}

	/**
	 * Converts a Gherkin location to an Eclipse document position.
	 * 
	 * @param location the Gherkin location
	 * @return the corresponding Eclipse position
	 * @throws BadLocationException if the location is invalid
	 */
	public Position getPosition(io.cucumber.messages.types.Location location) throws BadLocationException {
		return getPosition(location, 0);
	}

	/**
	 * Converts a Gherkin location to an Eclipse document position with line offset adjustment.
	 * 
	 * @param location   the Gherkin location
	 * @param lineOffset offset to subtract from the line number
	 * @return the corresponding Eclipse position
	 * @throws BadLocationException if the location is invalid
	 */
	public Position getPosition(io.cucumber.messages.types.Location location, int lineOffset)
			throws BadLocationException {
		int line =  location.getLine().intValue();
		int offset = document.getLineOffset(line - 1 - lineOffset);
		return new Position(offset + location.getColumn().orElse(0l).intValue() - 1, 1);
	}

	/**
	 * Gets the end-of-line position for the specified Gherkin location.
	 * Handles different line ending characters (CR, LF, CRLF).
	 * 
	 * @param location the Gherkin location
	 * @return the position at the end of the line
	 * @throws BadLocationException if the location is invalid
	 */
	public Position getEolPosition(io.cucumber.messages.types.Location location) throws BadLocationException {
		int line = location.getLine().intValue();
		int offset = document.getLineOffset(line - 1);
		int lineLength = document.getLineLength(line - 1);
		// Workaround for Bug 570740
		if (lineLength == 0) {
			return new Position(offset + lineLength, 1);
		}
		if (lineLength == 1) {
			char c = document.get(offset, 1).charAt(0);
			if (c == '\n' || c == '\r') {
				return new Position(offset, 1);
			} else {
				return new Position(offset + 1, 1);
			}
		}
		int eolOffset;
		char c = document.get(offset + lineLength - 2, 1).charAt(0);
		if (c == '\r') {
			eolOffset = 2;
		} else {
			eolOffset = 1;
		}
		return new Position(offset + lineLength - eolOffset, 1);
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
	 * Finds the longest keyword that matches the beginning of the given line.
	 * Only checks step keywords (Given, When, Then, And, But).
	 * 
	 * @param line the line to check
	 * @return the longest matching keyword, or empty if no match
	 */
	public Optional<GherkinKeyword> getKeyWordOfLine(String line) {
		String typed = line.stripLeading();
		Optional<GherkinKeyword> keywordPrefix = getStepElementKeywords()
				.sorted(Collections.reverseOrder((s1, s2) -> s1.getKey().length() - s2.getKey().length()))
				.filter(keyWord -> typed.startsWith(keyWord.getKey() + " ")).findFirst();
		return keywordPrefix;
	}

	/**
	 * Creates a stream of Gherkin keywords using the provided accessor function.
	 * Filters out wildcard keywords (*) and trims whitespace.
	 * 
	 * @param keyWords function to extract keywords from the dialect
	 * @return a stream of {@link GherkinKeyword}s for the current dialect
	 */
	public Stream<GherkinKeyword> keyWords(Function<GherkinDialect, List<String>> keyWords) {
		return keyWords.apply(dialect).stream().map(s -> s.trim())
				.filter(Predicate.not(GherkinEditorDocument::isWildcard))
				.map(str -> new GherkinKeyword(str, locale, dialect));
	}

	/**
	 * @return the resource of the document or <code>null</code> if it can't be
	 *         determined
	 */
	public IResource getResource() {
		return resourceSupplier.get();
	}

	/**
	 * Creates a new GherkinEditorDocument instance.
	 * Package-private factory method for use by {@link GherkinEditorDocumentManager}.
	 * 
	 * @param document the document to parse
	 * @param resource supplier for the associated resource, or null if none
	 * @return a new GherkinEditorDocument instance
	 */
	static GherkinEditorDocument create(IDocument document, Supplier<IResource> resource) {
		return new GherkinEditorDocument(document, resource);
	}

	private static boolean isWildcard(String keyword) {
		return "*".equals(keyword);
	}

}
