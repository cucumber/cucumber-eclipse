package io.cucumber.eclipse.editor.document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
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
 * 
 * Defines unified access from the editor to the parsed content
 * 
 * @author christoph
 *
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

	private static final ConcurrentHashMap<IDocument, GherkinEditorDocument> DOCUMENT_MAP = new ConcurrentHashMap<>();
	private volatile boolean dirty;
	private final IDocument document;
	private final GherkinDialect dialect;
	private final Locale locale;

	private Supplier<IResource> resourceSupplier;

	private GherkinEditorDocument(IDocument document, Supplier<IResource> resourceSupplier) {

		super(getEnvelopes(document, resourceSupplier));
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

	private static Envelope[] getEnvelopes(IDocument document, Supplier<IResource> resourceSupplier) {
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

	public IDocument getDocument() {
		return document;
	}

	public Position getPosition(io.cucumber.messages.types.Location location) throws BadLocationException {
		return getPosition(location, 0);
	}

	public Position getPosition(io.cucumber.messages.types.Location location, int lineOffset)
			throws BadLocationException {
		int line =  location.getLine().intValue();
		int offset = document.getLineOffset(line - 1 - lineOffset);
		return new Position(offset + location.getColumn().orElse(0l).intValue() - 1, 1);
	}

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
	 * Computes the longest keyword prefix the given line
	 * 
	 * @param line the line to check
	 * @return the longest match for the given line
	 */
	public Optional<GherkinKeyword> getKeyWordOfLine(String line) {
		String typed = line.stripLeading();
		Optional<GherkinKeyword> keywordPrefix = getStepElementKeywords()
				.sorted(Collections.reverseOrder((s1, s2) -> s1.getKey().length() - s2.getKey().length()))
				.filter(keyWord -> typed.startsWith(keyWord.getKey() + " ")).findFirst();
		return keywordPrefix;
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
		if (isCompatible(document)) {
			return DOCUMENT_MAP.compute(document, (key, value) -> {
				if (value == null || value.dirty) {
					return parse(key, () -> resourceForDocument(key));
				}
				return value;
			});
		}
		return null;
	}

	/**
	 * if the resource is currently managed by TextFileBufferManager returns the
	 * cached instance of the corresponding document, otherwise create a detached
	 * copy of the resource if it is convertible otherwise <code>null</code> is
	 * returned
	 * 
	 * @param resource the resource to request a gherking document
	 * @return {@link GherkinEditorDocument} for the given resource
	 */
	public static GherkinEditorDocument get(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(),
					LocationKind.IFILE);
			if (buffer != null) {
				return get(buffer.getDocument());
			}
			try {
				try (InputStream stream = file.getContents()) {
					return parse(new Document(IOUtils.toString(stream, file.getCharset())), () -> file);
				}
			} catch (IOException e) {
				return null;
			} catch (CoreException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param document the document to check
	 * @return true if the document is currently cached
	 */
	public static boolean has(IDocument document) {
		return DOCUMENT_MAP.contains(document);
	}

	/**
	 * Checks if the given document is a {@link GherkinEditorDocument} comptible one
	 * 
	 * @param document the document to check
	 * @return <code>true</code> if the given document is one that could be used
	 *         with {@link GherkinEditorDocument}
	 */
	public static boolean isCompatible(IDocument document) {
		if (document != null) {
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
			if (buffer != null) {
				try {
					IContentType contentType = buffer.getContentType();
					if (contentType != null) {
						return "io.cucumber.eclipse.editor.content-type.feature".equals(contentType.getId());
					}
				} catch (CoreException e) {
				}
			}
		}
		return false;
	}

	/**
	 * @return the resource of the document or <code>null</code> if it can't be
	 *         determined
	 */
	public IResource getResource() {
		return resourceSupplier.get();
	}

	/**
	 * Parses the string into a (temporary) document
	 * 
	 * @param document the document to parse
	 * @return a detached {@link GherkinEditorDocument} instance
	 */
	public static GherkinEditorDocument parse(IDocument document, Supplier<IResource> resource) {
		return new GherkinEditorDocument(document, resource);
	}

	public static IResource resourceForDocument(IDocument document) {
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

	private static boolean isWildcard(String keyword) {
		return "*".equals(keyword);
	}

}
