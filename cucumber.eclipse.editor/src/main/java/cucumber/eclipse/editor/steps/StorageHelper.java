package cucumber.eclipse.editor.steps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class StorageHelper {

	protected static void saveIntoBuildDirectory(String filename, IProject project, IProgressMonitor monitor,
			byte[] data) throws CoreException {
		IFolder target = project.getFolder("target"); // should be replace by a project preference
		if (!target.exists()) {
			target.create(false, true, monitor);
		}
		IFile buildFile = target.getFile(filename);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		if (buildFile.exists()) {
			buildFile.setContents(inputStream, true, false, monitor);
		} else {
			buildFile.create(inputStream, true, monitor);
		}
	}
	
	protected static String copy(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] byteArray = buffer.toByteArray();

		String stepDefinitionsRepositorySerialized = new String(byteArray, StandardCharsets.UTF_8);
		return stepDefinitionsRepositorySerialized;
	}

}
