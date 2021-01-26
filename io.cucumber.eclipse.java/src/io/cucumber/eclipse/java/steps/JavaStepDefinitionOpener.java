package io.cucumber.eclipse.java.steps;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.validation.CucumberGlueValidator;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStep;

@Component(service = IStepDefinitionOpener.class)
public class JavaStepDefinitionOpener implements IStepDefinitionOpener {

	private void showMethod(IMember member) throws PartInitException, JavaModelException {
		ICompilationUnit cu = member.getCompilationUnit();
		IEditorPart javaEditor = JavaUI.openInEditor(cu);
		JavaUI.revealInEditor(javaEditor, (IJavaElement) member);
	}

	@Override
	public boolean openInEditor(ITextViewer textViewer, IResource resource, Step step) throws CoreException {
		IJavaProject project = JDTUtil.getJavaProject(resource);
		if (project == null) {
			return false;
		}
		AtomicReference<IMethod> resolvedMethod = new AtomicReference<>();
		Display display = textViewer.getTextWidget().getDisplay();
		BusyIndicator.showWhile(display, () -> {
			AtomicBoolean done = new AtomicBoolean();
			Job job = Job.create("Search for step '" + step.getText() + "'", new ICoreRunnable() {

				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						Collection<MatchedStep> steps = CucumberGlueValidator.getMatchedSteps(textViewer.getDocument(),
								monitor);

						CucumberCodeLocation location = steps.stream()
								.filter(matched -> step.getLocation().getLine() == matched.getLocation().getLine())
								.filter(matched -> {
									TestStep testStep = matched.getTestStep();
									if (testStep instanceof PickleStepTestStep) {
										io.cucumber.plugin.event.Step pickleStep = ((PickleStepTestStep) testStep)
												.getStep();
										return pickleStep.getText().equalsIgnoreCase(step.getText());
									}
									return false;
								}).map(matched -> matched.getCodeLocation()).findFirst().orElse(null);
						if (location != null) {
							resolvedMethod.set(JDTUtil.resolveMethod(project, location, monitor));
						}
					} catch (InterruptedException e) {
					} finally {
						done.set(true);
					}
				}
			});
			job.schedule();
			while (!done.get() && !display.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		});
		IMethod method = resolvedMethod.get();
		if (method != null) {
			showMethod(method);
		}
		return method != null;
	}

	@Override
	public boolean canOpen(IResource resource) throws CoreException {
		return JDTUtil.getJavaProject(resource) != null;
	}

}
