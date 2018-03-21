package cucumber.eclipse.steps.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An AST parser to parse Class/imports/Constructor/Methods from a Java File
 * 
 * @author girija.panda@nokia.com
 */

public class JavaParser extends AbstractHandler {

	private ASTParser astParser = null;
	private CompilationUnit compUnit = null;
	private ASTRequestor astRequestor = null;

	private String className = null;
	private String methodBody = null;

	// Initialize ASTParser as CompilationUnit
	@SuppressWarnings("deprecation")
	public JavaParser(ICompilationUnit iCompilationUnit, IProgressMonitor progressMonitor) {

		// astParser = ASTParser.newParser(AST.JLS3); //for jdk-7
		this.astParser = ASTParser.newParser(AST.JLS8); // for jdk-8
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		// parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);

		// For unused import declarations[TBD]
		astParser.setCompilerOptions(Collections.singletonMap(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.ERROR));
		astParser.setSource(iCompilationUnit);
		astParser.setResolveBindings(true);
		compUnit = (CompilationUnit) astParser.createAST(progressMonitor);
	}

	/**
	 * Parse and Collect Method Details From Java file
	 * 
	 * @param compUnit
	 * @return List<MethodDeclaration>
	 * @throws JavaModelException
	 */
	public List<MethodDeclaration> getAllMethods() throws JavaModelException {
		// Visit and parse Methods from CompilationUnit
		MethodVisitor visitor = new MethodVisitor();
		compUnit.accept(visitor);
		return visitor.getMethods();
	}

	/**
	 * @param method
	 * @return String
	 */
	public String getMethodBody(MethodDeclaration method) {
		this.methodBody = method.getBody().toString();
		return methodBody;
	}
	

	/**
	 * @param iCompUnit
	 * @return String
	 */
	public String getClassName(ICompilationUnit iCompUnit) {
		this.className = iCompUnit.getResource().getName();
		return className;
	}

	/**
	 * Get Line-Number of statement
	 * 
	 * @param statement
	 * @return int
	 */
	public int getLineNumber(Statement statement) {
		return compUnit.getLineNumber(statement.getStartPosition());
	}


	/**
	 * Method Visitor
	 * 
	 * @author giriija.panda@nokia.com
	 */
	public class MethodVisitor extends ASTVisitor {

		List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();

		@Override
		public boolean visit(MethodDeclaration node) {
			methods.add(node);
			return super.visit(node);
		}

		// Get Method Declarations
		public List<MethodDeclaration> getMethods() {
			return methods;
		}

	}

	/**
	 * Get All Unused imports from a java file
	 * 
	 * @param compUnit
	 * @return Set<String>
	 * @throws JavaModelException
	 */
	public Set<String> getUnusedImports(ICompilationUnit icompUnit) throws JavaModelException {

		final Set<String> unusedImports = new HashSet<String>();
		astRequestor = new ASTRequestor() {
			@Override
			public void acceptAST(ICompilationUnit sourceUnit, CompilationUnit compiledUnit) {
				// Visit all the compiler problems looking for unused imports.
				// final Set<String> unusedImports = new HashSet<String>();
				IProblem[] problems = compiledUnit.getProblems();
				boolean onlyUnusedImportErrors = true;
				for (IProblem problem : problems) {
					int id = problem.getID();
					if (id == IProblem.UnusedImport) {
						unusedImports.add(problem.getArguments()[0]);
					} else {
						// If there are other errors, we can't rely on there
						// being unused import errors because they're optional
						// and aren't produced when non-optional errors are
						// present.
						onlyUnusedImportErrors = false;
						break;
					}
				}
				// If there are other errors, we need to do our own detailed
				// analysis to find unused imports...
				if (!onlyUnusedImportErrors) {
					// Build up the set up all imported names, ignoring static
					// and on-demand imports.
					@SuppressWarnings({ "unchecked", "cast" })
					List<? extends ImportDeclaration> imports = (List<? extends ImportDeclaration>) compiledUnit
							.imports();
					for (ImportDeclaration importDeclaration : imports) {
						if (!importDeclaration.isStatic() && !importDeclaration.isOnDemand()) {
							unusedImports.add(importDeclaration.getName().getFullyQualifiedName());
						}
					}
				}
			}

		};

		// Compile the working copy source, applying the unused import remover.
		astParser.createASTs(new ICompilationUnit[] { icompUnit }, new String[0], astRequestor, null);
		return unusedImports;
	}

	/**
	 * Get Class Names
	 * 
	 * @param compUnit
	 * @return
	 */
	public String getClassName(CompilationUnit compUnit) {

		compUnit.accept(new ASTVisitor() {
			String packageName = "";

			public boolean visit(TypeDeclaration node) {
				className = node.getName().getFullyQualifiedName();
				if (packageName != "") {
					// String fullyQualifiedClassName = packageName + "." +
					// className;
					System.out.println("Fully Qualified ClassName : " + packageName + "." + className);
				} else {
					System.out.println("Class Name : " + className);
				}
				return false;
			}

			public boolean visit(PackageDeclaration node) {
				packageName = node.getName().getFullyQualifiedName();
				return false;
			}
		});
		return className;
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

}
