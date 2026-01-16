package io.cucumber.eclipse.java.steps;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;
import io.cucumber.eclipse.editor.validation.DocumentValidator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;
import io.cucumber.eclipse.java.plugins.MatchedPickleStep;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.validation.JavaGlueStore;
import io.cucumber.messages.types.Step;

@Component(service = IStepDefinitionOpener.class)
public class JavaStepDefinitionOpener implements IStepDefinitionOpener {

	private JavaGlueStore glueStore;

	@Activate
	public JavaStepDefinitionOpener(@Reference JavaGlueStore glueValidatorService) {
		this.glueStore = glueValidatorService;
	}

	public static void showMethod(IMethod[] methods, Shell shell) {
		if (methods == null || methods.length == 0) {
			return;
		}
		try {
			if (methods.length == 1) {
				open(methods[0]);
			} else {
				EditorLogging.error("More than one method matches: " + Arrays.toString(methods));
			}
		} catch (PartInitException | JavaModelException e) {
			EditorLogging.error("Open target method failed", e);
		}
	}

	private static void open(IMethod method) throws PartInitException, JavaModelException {
		ICompilationUnit cu = method.getCompilationUnit();
		IEditorPart javaEditor = JavaUI.openInEditor(cu);
		JavaUI.revealInEditor(javaEditor, (IJavaElement) method);
	}

	@Override
	public boolean openInEditor(ITextViewer textViewer, IResource resource, Step step) throws CoreException {
		IJavaProject project = JDTUtil.getJavaProject(resource);
		if (project == null) {
			return false;
		}
		AtomicReference<IMethod[]> resolvedMethods = new AtomicReference<>();
		AtomicBoolean cancelled = new AtomicBoolean(false);
		Display display = textViewer.getTextWidget().getDisplay();
		IDocument document = textViewer.getDocument();
		BusyIndicator.showWhile(display, () -> {
			AtomicBoolean done = new AtomicBoolean();
			AtomicReference<Job> jobRef = new AtomicReference<>();
			Listener escapeListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (event.type == SWT.KeyDown && event.keyCode == SWT.ESC) {
						Job job = jobRef.get();
						if (job != null) {
							job.cancel();
							cancelled.set(true);
							done.set(true);
							display.wake();
						}
					}
				}
			};
			display.addFilter(SWT.KeyDown, escapeListener);
			Job job = Job.create("Search for step '" + step.getText() + "' (press ESC to cancel)", new ICoreRunnable() {

				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						DocumentValidator.joinValidation(project.getProject());
					} catch (OperationCanceledException | InterruptedException e) {
						cancelled.set(true);
						display.wake();
						return;
					}
					try {
						Collection<MatchedStep<?>> steps = glueStore.getMatchedSteps(document);
						StringBuilder sb = new StringBuilder();
						sb.append("step '");
						sb.append(step.getText());
						sb.append("' line: ");
						sb.append(step.getLocation().getLine());
						sb.append("\r\nmatched steps are:\r\n");
						for (MatchedStep<?> matched : steps) {
							if (matched instanceof MatchedPickleStep) {
								MatchedPickleStep pickleStep = (MatchedPickleStep) matched;
								sb.append(
										"pickleStep.getTestStep().getPattern='" + pickleStep.getTestStep().getPattern()
										+ " -> "
												+ "' ["
										+ pickleStep.getCodeLocation() + "]");
								sb.append(" line: " + matched.getLocation().getLine());
								sb.append("  getTestStep().getStep().getText='"
										+ pickleStep.getTestStep().getStep().getText());
								sb.append("'\r\n");
							}
						}
						sb.append(" found -> ");

						CucumberCodeLocation location = steps.stream()
								.filter(matched -> step.getLocation().getLine() == matched.getLocation().getLine())
								.filter(MatchedPickleStep.class::isInstance).map(MatchedPickleStep.class::cast)
						// FIXME we need the data from the messages here....
//								.filter(matched -> matched.getTestStep().getStep().getText()
//										.equalsIgnoreCase(step.getText()))
								.map(matched -> matched.getCodeLocation()).findFirst().orElse(null);
						
						if (Tracing.DEBUG_STEPS_ENABLED) {
							sb.append(location != null ? location.toString() : "null");
							Tracing.get().trace(Tracing.DEBUG_STEPS, sb.toString());
						}
						
						if (location != null) {
							resolvedMethods.set(JDTUtil.resolveMethod(project, location, monitor));
						}
					} finally {
						cancelled.set(cancelled.get() || monitor.isCanceled());
						done.set(true);
						display.wake();
					}
				}
			});
			jobRef.set(job);
			job.schedule();
			try {
				while (!done.get() && !display.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			} finally {
				display.removeFilter(SWT.KeyDown, escapeListener);
			}
		});
		if (cancelled.get()) {
			return true;
		}
		IMethod[] method = resolvedMethods.get();
		if (method != null) {
			showMethod(method, textViewer.getTextWidget().getShell());
		}
		return method != null && method.length > 0;
	}

	@Override
	public boolean canOpen(IResource resource) throws CoreException {
		return JDTUtil.getJavaProject(resource) != null;
	}

}
