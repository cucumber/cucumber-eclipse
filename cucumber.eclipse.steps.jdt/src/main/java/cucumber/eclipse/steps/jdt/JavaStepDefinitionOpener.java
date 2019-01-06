package cucumber.eclipse.steps.jdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

import cucumber.eclipse.steps.integration.IStepDefinitionOpener;
import cucumber.eclipse.steps.integration.StepDefinition;

public class JavaStepDefinitionOpener implements IStepDefinitionOpener {

	@Override
	public void openInEditor(StepDefinition stepDefinition) throws CoreException {
		if(canOpen(stepDefinition)) {
			try {
				showMethod(getMember(stepDefinition));
			} catch (PartInitException e) {
				throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			} catch (JavaModelException e) {
				throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

	@Override
	public boolean canOpen(StepDefinition stepDefinition) {
		IMember member = this.getMember(stepDefinition);
		return member != null;
	}

	private void showMethod(IMember member) throws PartInitException, JavaModelException {
		JavaUI.openInEditor(member, true, true);
//        ICompilationUnit cu = member.getCompilationUnit();
//        IEditorPart javaEditor = JavaUI.openInEditor(cu);
//        JavaUI.revealInEditor(javaEditor, (IJavaElement)member);
    }
	
	private IMember getMember(StepDefinition stepDefinition) {
		String jdtHandleIdentifier = stepDefinition.getJDTHandleIdentifier();
		if(jdtHandleIdentifier == null || jdtHandleIdentifier.isEmpty()) {
			return null;
		}
		IJavaElement element = JavaCore.create(jdtHandleIdentifier);
		if(! (element instanceof IMember)) {
			return null;
		}
		return (IMember) element;
	}
	
}
