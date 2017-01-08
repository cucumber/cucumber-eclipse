package cucumber.eclipse.steps.jdt;

import gherkin.formatter.model.Step;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import cucumber.eclipse.steps.integration.IStepGenerator;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.snippets.CamelCaseConcatenator;
import cucumber.runtime.snippets.FunctionNameGenerator;

public class StepGenerator implements IStepGenerator {

	@Override
	public boolean supports(IFile file) {
		return file.getName().endsWith(".java");
	}

	@Override
	public TextEdit createStepSnippet(IFile stepFile, Step step) throws IOException, CoreException {
		Backend backend = new JavaBackend(new EmptyObjectFactory(), new EmptyClassFinder());
		
		String snippetText = backend.getSnippet(step, new FunctionNameGenerator(new CamelCaseConcatenator()));

		IDocument document = read(stepFile.getContents(), stepFile.getCharset());
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(document.get().toCharArray());
		
		CompilationUnit target = (CompilationUnit) parser.createAST(null);
		target.recordModifications();
		
		TypeDeclaration targetType = (TypeDeclaration) target.types().get(0);
		
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(snippetText.toCharArray());
		TypeDeclaration fragmentContainer = (TypeDeclaration) parser.createAST(null);
		
		for (Object fragmentObject : fragmentContainer.bodyDeclarations()) {
			BodyDeclaration fragmentNode = (BodyDeclaration) fragmentObject;
		
			// clone into target AST
			fragmentNode = (BodyDeclaration) ASTNode.copySubtree(target.getAST(), fragmentNode);
		
			@SuppressWarnings("unchecked")
			List<BodyDeclaration> bodyDeclarations = targetType.bodyDeclarations();
			
			bodyDeclarations.add(getInsertionPoint(targetType, fragmentNode), fragmentNode);
		}
		
		return target.rewrite(document, null);
	}
	
	private static IDocument read(InputStream in, String encoding) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		int n;
		byte[] b = new byte[16384];
		
		while ((n = in.read(b, 0, b.length)) != -1) {
			out.write(b, 0, n);
		}
		
		out.flush();
		
		return new Document(new String(out.toByteArray(), encoding));
	}
	
	private static int getInsertionPoint(TypeDeclaration source, BodyDeclaration fragment) {
		if (source.bodyDeclarations().isEmpty()) {
			return 0;
		}

		if (fragment.getNodeType() == ASTNode.METHOD_DECLARATION) {
			int last = getLastFragmentOfTypePosition(source, fragment.getNodeType(),
				fragment.getModifiers());

			if (last >= 0) {
				return last + 1;
			}

			int afterFields = getLastFragmentOfTypePosition(source, ASTNode.FIELD_DECLARATION);
			return (afterFields >= 0) ? afterFields + 1 : 0;
		}

		return 0;
	}

	private static int getLastFragmentOfTypePosition(TypeDeclaration source, int nodeType) {
		return getLastFragmentOfTypePosition(source, nodeType, -1);
	}

	private static int getLastFragmentOfTypePosition(TypeDeclaration source, int nodeType, int modifiers) {
		for (int i = source.bodyDeclarations().size() - 1; i >= 0; i --) {
			BodyDeclaration bodyDecl = (BodyDeclaration) source.bodyDeclarations().get(i);

			if (bodyDecl.getNodeType() == nodeType && (modifiers == -1 || bodyDecl.getModifiers() == modifiers)) {
				return i;
			}
		}

		return -1;
	}

	private static class EmptyObjectFactory implements ObjectFactory {
		@Override
		public void start() {
			// no action
		}
	
		@Override
		public void stop() {
			// no action
		}
	
		@Override
		public void addClass(Class<?> glueClass) {
			// no action
		}
	
		@Override
		public <T> T getInstance(Class<T> glueClass) {
			return null;
		}
	}

	private static class EmptyClassFinder implements ClassFinder {
		@Override
		public <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName) {
			return new ArrayList<Class<? extends T>>();
		}
	}	
}
