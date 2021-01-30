package io.cucumber.eclipse.java.launching;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

import io.cucumber.core.resource.Resource;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;

/**
 * Resource based on documents
 * 
 * @author christoph
 *
 */
public class DocumentResource implements Resource {

	private IDocument document;
	private URI uri;

	public DocumentResource(GherkinEditorDocument document) {
		this.document = document.getDocument();
		IResource resource = document.getResource();
		if (resource != null) {
			uri = Objects.requireNonNullElseGet(resource.getLocationURI(), () -> resource.getRawLocationURI());
		} else {
			try {
				uri = new URI("document:/" + System.identityHashCode(document));
			} catch (URISyntaxException e) {
				throw new AssertionError("should never happen", e);
			}
		}

	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(document.get().getBytes(StandardCharsets.UTF_8));
	}

}
