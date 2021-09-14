package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import io.cucumber.core.resource.Resource;

public class FileResource implements Resource {

	private IFile file;

	public FileResource(IFile file) {
		this.file = file;
	}

	@Override
	public URI getUri() {
		return Objects.requireNonNullElseGet(file.getLocationURI(), () -> file.getRawLocationURI());
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return file.getContents();
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

}
