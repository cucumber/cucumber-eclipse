package io.cucumber.eclipse.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import io.cucumber.eclipse.editor.steps.ExpressionDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepParameter;

public class StorageHelper {

	private static final String OUTPUT_FOLDER = ".cucumber";

	public static InputStream toStream(Serializable serializable, IProgressMonitor monitor) throws IOException {
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
			try (ObjectOutputStream outputStream = new ObjectOutputStream(bout)) {
				outputStream.writeObject(serializable);
			}
			return new ByteArrayInputStream(bout.toByteArray());
		}
	}
	
	public static <T extends Serializable> T fromStream(Class<T> type, InputStream stream, IProgressMonitor monitor) throws IOException, ClassNotFoundException {
		try (ObjectInputStream objectStream = new ObjectInputStream(stream)) {
			return type.cast(objectStream.readObject());
		}
	}

	public static IFolder getOutputFolder(IProject project)
			throws JavaModelException, CoreException {
		if (!project.isOpen()) {
			throw new IllegalStateException("Project is closed");
		}
		IFolder folder;
		if (project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);
			folder = project.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation()).getFolder(OUTPUT_FOLDER);
		} else {
			folder = project.getFolder(OUTPUT_FOLDER);
		}
		return folder;
	}

	public static void saveIntoBuildDirectory(String filename, IProject project, IProgressMonitor monitor,
			InputStream stream) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Saving data", 100);
		IFolder target = getOutputFolder(project);
		createFolder(target, subMonitor.newChild(10));
		IFile buildFile = target.getFile(filename);
		if (buildFile.exists()) {
			buildFile.setContents(stream, true, false, subMonitor.newChild(90));
		} else {
			buildFile.create(stream, true, subMonitor.newChild(90));
		}
	}

	private static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
		if (folder.exists()) {
			return;
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			IFolder parentFolder = (IFolder) parent;
			createFolder(parentFolder, subMonitor.newChild(1));
		}
		folder.create(true, true, subMonitor.newChild(1));
	}


	public static void writeStepDefinition(StepDefinition stepDefinition, ObjectOutput out) throws IOException {
		out.writeObject(stepDefinition.getId());
		out.writeObject(stepDefinition.getLabel());
		out.writeObject(stepDefinition.getPackageName());
		out.writeObject(stepDefinition.getSourceName());
		out.writeInt(stepDefinition.getLineNumber());
		out.writeObject(stepDefinition.getExpression().getLang());
		out.writeObject(stepDefinition.getExpression().getText());
		IResource source = stepDefinition.getSource();
		if (source != null) {
			out.writeObject(source.getFullPath().toString());
		} else {
			out.writeObject(null);
		}
	}

	public static StepDefinition readStepDefinition(ObjectInput in) throws ClassNotFoundException, IOException {
		String id = (String) in.readObject();
		String label = (String) in.readObject();
		String packageName = (String) in.readObject();
		String sourceName = (String) in.readObject();
		int line = in.readInt();
		String expLang = (String) in.readObject();
		String expStr = (String) in.readObject();
		String sourceRef = (String) in.readObject();
		IResource resource = ResourceHelper.find(sourceRef);
		ExpressionDefinition expression = new ExpressionDefinition(expStr, expLang);
		// FIXME
		return new StepDefinition(id, label, expression, resource, line, sourceName, packageName, new StepParameter[0]);
	}

}
