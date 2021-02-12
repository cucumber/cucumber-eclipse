package io.cucumber.eclipse.java.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.steps.ParameterType;
import io.cucumber.eclipse.editor.steps.StepParameter;
import io.cucumber.eclipse.java.CucumberAnnotation;
import io.cucumber.eclipse.java.JDTUtil;

/**
 * Find step definitions on Java elements.
 * 
 * @author qvdk
 *
 */
public abstract class JavaStepDefinitionsProvider implements IStepDefinitionsProvider {

	protected static final Pattern cucumberApiAnnotationMatcher = Pattern
			.compile("cucumber\\.api\\.java\\.([a-z_]+)\\.(.*)$");

	public static final Pattern ioCucumberAnnotationMatcher = Pattern
			.compile("io\\.cucumber\\.java\\.([a-z_]+)\\.(.*)$");

	protected static List<CucumberAnnotation> getAllAnnotationsInPackage(final IJavaProject javaProject,
			final String packageFrag, final String lang, IProgressMonitor monitor)
			throws CoreException, JavaModelException {

		SearchPattern pattern = SearchPattern.createPattern(packageFrag, IJavaSearchConstants.PACKAGE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE);

		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaProject.getPackageFragments());

		final List<CucumberAnnotation> annotations = new ArrayList<CucumberAnnotation>();

		SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) {
				try {
					if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
						IPackageFragment frag = (IPackageFragment) match.getElement();
						for (IClassFile cls : frag.getOrdinaryClassFiles()) {
							IType t = cls.findPrimaryType();
							if (t.isAnnotation()) {
								annotations.add(new CucumberAnnotation(t.getElementName(), lang));
							}
						}
					}
				} catch (JavaModelException e) {
				}
			}
		};
		SearchEngine engine = new SearchEngine();
		jdtSearch(engine, pattern, scope, requestor, monitor);
		return annotations;
	}

	protected static void jdtSearch(SearchEngine engine, SearchPattern pattern, IJavaSearchScope scope,
			SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					requestor, null);
		} catch (Throwable t) {
			t.printStackTrace();
			// if the search engine failed, skip it is a bug into the JDT plugin
		}
	}

	/**
	 * @param compUnit
	 * @param annotation
	 * @return int
	 * @throws JavaModelException
	 */
	protected static int getLineNumber(ICompilationUnit compUnit, ISourceReference annotation)
			throws JavaModelException {
		Document document = new Document(compUnit.getBuffer().getContents());
		try {
			return document.getLineOfOffset(annotation.getSourceRange().getOffset()) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}

	/**
	 * @param importedAnnotations
	 * @param annotation
	 * @return CucumberAnnotation
	 * @throws JavaModelException
	 */
	protected static CucumberAnnotation getCukeAnnotation(List<CucumberAnnotation> importedAnnotations,
			IAnnotation annotation) throws JavaModelException {

		Matcher m = cucumberApiAnnotationMatcher.matcher(annotation.getElementName());
		if (m.find()) {
			return new CucumberAnnotation(m.group(2), m.group(1));
		}
		m = ioCucumberAnnotationMatcher.matcher(annotation.getElementName());
		if (m.find()) {
			return new CucumberAnnotation(m.group(2), m.group(1));
		}
		for (CucumberAnnotation cuke : importedAnnotations) {
			if (cuke.getAnnotation().equals(annotation.getElementName()))
				return cuke;
		}
		return null;
	}

	/**
	 * @param annotation
	 * @return String
	 * @throws JavaModelException
	 */
	protected static String getAnnotationText(IAnnotation annotation) throws JavaModelException {
		for (IMemberValuePair mvp : annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}

	@Override
	public boolean support(IResource resource) throws CoreException {
		if (resource instanceof IProject) {
			return JDTUtil.getJavaProject(resource) != null;
		}
		return false;
	}

	protected static StepParameter[] getParameter(IMethod method) throws JavaModelException {

		return getParameterInternal(method, Runtime.getRuntime().availableProcessors());
	}

	private static StepParameter[] getParameterInternal(IMethod method, int retry) throws JavaModelException {
		ILocalVariable[] parameters = method.getParameters();
		StepParameter[] stepParameters = new StepParameter[parameters.length];
		for (int i = 0; i < stepParameters.length; i++) {
			ILocalVariable parameterVariable = parameters[i];
			String name = parameterVariable.getTypeSignature();
			String simpleName = Signature.getSignatureSimpleName(name);
			String[] values = null;
			if (simpleName != null) {
				String[][] resolved = JDTUtil.resolveTypeWithRetry(method.getDeclaringType().getDeclaringType(),
						simpleName);
				if (resolved != null) {
					String qualifiedName = Signature.toQualifiedName(resolved[0]);
					IType type = method.getJavaProject().findType(qualifiedName);
					if (type.isEnum()) {
						IField[] fields = type.getFields();
						List<String> valuesList = new ArrayList<>();
						for (IField field : fields) {
							if (field.isEnumConstant()) {
								valuesList.add(field.getElementName());
							}
						}
						values = valuesList.toArray(String[]::new);
					}
				}
			}
			stepParameters[i] = new StepParameter(parameterVariable.getElementName(), ParameterType.UNKNWON, values);
		}
		return stepParameters;
	}
}
