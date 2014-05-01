package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.EditorPluginAction;
import org.eclipse.ui.texteditor.IDocumentProvider;

import cucumber.eclipse.steps.integration.Step;


public class PopupMenuFindStepActionDelegate implements IEditorActionDelegate {

	
	private Editor editorPart;
	private Pattern cukePattern = Pattern.compile("(?:Given|When|Then) (.*)$");
	
	@Override
	public void run(IAction action) {

		IProject project = 
				((IFileEditorInput) editorPart.getEditorInput()).getFile().getProject();
		
		List<Step> steps = getAllDeclaredSteps(project);
		
		String selectedLine = getSelectedLine();
		
		Matcher matcher = cukePattern.matcher(selectedLine);
		if (matcher.matches()) {
			String cukeStep = matcher.group(1);
		
			for(Step step: steps) {
				if (step.matches(cukeStep)) {
					try {
						openEditor(step);
					} catch (Exception e) {
						
					}
				}
			}
			
			System.out.println(cukeStep);
		}
		
		System.out.println(steps.toString());
	}
	
	private void openEditor(Step step) throws CoreException {
		   IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		   IFile file = root.getFile(step.getSource().getFullPath());
		   IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		   HashMap<String, Integer> map = new HashMap<String, Integer>();
		   map.put(IMarker.LINE_NUMBER, step.getLineNumber());
		   IMarker marker = file.createMarker(IMarker.TEXT);
		   marker.setAttributes(map);
		   IDE.openEditor(page, marker);
		   marker.delete();
	}
	

	private String getSelectedLine() {
		TextSelection selecton = (TextSelection) editorPart.getSelectionProvider().getSelection();
		int line = selecton.getStartLine();	
		
		IDocumentProvider docProvider = editorPart.getDocumentProvider();
		IDocument doc = docProvider.getDocument(editorPart.getEditorInput());
		
		try {
			return doc.get(doc.getLineOffset(line), doc.getLineLength(line)).trim();
		} catch (BadLocationException e) {
			return "";
		}
	}

	private List<Step> getAllDeclaredSteps(IProject project) {
		List<Step> steps = new LinkedList<Step>();

		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);

				IPackageFragment[] packages = javaProject.getPackageFragments();
				for (IPackageFragment javaPackage : packages) {

					if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

						System.out.println("Package "
								+ javaPackage.getElementName());

						for (ICompilationUnit compUnit : javaPackage
								.getCompilationUnits()) {
							steps.addAll(getCukeAnnotations(compUnit));
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} 
		return steps;
	}
	
	private List<Step> getCukeAnnotations(ICompilationUnit compUnit)
			throws JavaModelException{
		List<Step> steps = new ArrayList<Step>();
		
		for (IType t : compUnit.getTypes()) {
			for (IMethod method : t.getMethods()) {
				for (IAnnotation annotation : method.getAnnotations()) {
					if (isStepAnnotation(compUnit, annotation)) {
						Step step = new Step();
						step.setSource(method.getResource());
						step.setText(getAnnotationText(annotation));
						step.setLineNumber(getLineNumber(compUnit, annotation));
						steps.add(step);

					}
				}
			}
		}
		System.out.println(steps);
		return steps;

	}
	
	private int getLineNumber(ICompilationUnit compUnit, IAnnotation annotation) throws JavaModelException {
		Document document = new Document(compUnit.getBuffer().getContents()); 
		
		try {
			return document.getLineOfOffset(annotation.getSourceRange().getOffset()) + 1;
		} catch (BadLocationException e) {
			return 0;
		}
	}
	
	private boolean isStepAnnotation(ICompilationUnit compUnit,
			IAnnotation annotation) {
		
		List<String>  annotations = Arrays.asList("Given", "When", "Then");
		List<String>  fqAnnotations = Arrays.asList("cucumber.annotation.Given", "cucumber.annotation.When", "cucumber.annotation.Then");
		
		if (fqAnnotations.contains(annotation.getElementName())) return true;
		if (annotations.contains(annotation.getElementName())) {
			// TODO: Check imports
			return true;
		}
		return false; 
	}

	private String getAnnotationText(IAnnotation annotation) throws JavaModelException {
		for (IMemberValuePair mvp :  annotation.getMemberValuePairs()) {
			if (mvp.getValueKind() == IMemberValuePair.K_STRING) {
				return (String) mvp.getValue();
			}
		}
		return "";
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editorPart = (Editor) targetEditor;
	}

}
