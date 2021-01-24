package io.cucumber.eclipse.java.steps;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.steps.IStepDefinitionGenerator;

@Component(service = { IStepDefinitionGenerator.class })
public class StepGenerator implements IStepDefinitionGenerator {


	@Override
	public TextEdit createStepSnippet(String snippetText, IDocument targetDocument) throws IOException, CoreException {
		ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
		parser.setSource(targetDocument.get().toCharArray());
		
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
		
		return target.rewrite(targetDocument, null);
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

}
