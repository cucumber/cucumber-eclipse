package io.cucumber.eclipse.java.codemining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.cucumber.eclipse.editor.EditorLogging;
import io.cucumber.eclipse.editor.EditorReconciler;
import io.cucumber.eclipse.editor.SWTUtil;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.MatchedHookStep;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferences;
import io.cucumber.eclipse.java.steps.JavaStepDefinitionOpener;
import io.cucumber.eclipse.java.validation.JavaGlueValidatorService;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestStep;

/**
 * Provide java specific code minings to the editor
 * 
 * @author christoph
 *
 */
@Component(service = { /*
						 * This is actually registered through plugin.xml we only use the component to
						 * get the required services
						 */ }, immediate = true)
public class JavaReferencesCodeMiningProvider implements ICodeMiningProvider {

	private static AtomicReference<JavaGlueValidatorService> validationService = new AtomicReference<>();

	@Reference
	public void setJavaGlueValidatorService(JavaGlueValidatorService service) {
		validationService.set(service);
		EditorReconciler.reconcileAllFeatureEditors();
	}

	public void unsetJavaGlueValidatorService(JavaGlueValidatorService service) {
		if (validationService.compareAndSet(service, null)) {
			EditorReconciler.reconcileAllFeatureEditors();
		}
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				IDocument document = viewer.getDocument();
				IJavaProject javaProject = JDTUtil.getJavaProject(document);

				if (javaProject != null) {
					CucumberJavaPreferences preferences = CucumberJavaPreferences.of(javaProject.getProject());
					if (preferences.showHooks()) {
						JavaGlueValidatorService service = validationService.get();
						if (service != null) {
							Collection<MatchedStep<?>> steps = service.getMatchedSteps(document, monitor);
							if (!steps.isEmpty()) {
								return computeCodeMinings(document, javaProject, steps);
							}
						}
					}
				}
			} catch (OperationCanceledException e) {
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (CoreException e) {
				EditorLogging.error("Code mining failed", e);
			}
			return Collections.emptyList();
		});
	}

	private List<? extends ICodeMining> computeCodeMinings(IDocument document, IJavaProject javaProject,
			Collection<MatchedStep<?>> steps) {
		List<ICodeMining> list = new ArrayList<>();
		Map<Integer, List<MatchedHookStep>> stepByLine = steps.stream().filter(MatchedHookStep.class::isInstance)
				.map(MatchedHookStep.class::cast).collect(Collectors.groupingBy(step -> step.getLocation().getLine()));
		for (Entry<Integer, List<MatchedHookStep>> entry : stepByLine.entrySet()) {
			int lineNumber = entry.getKey() - 1;
			Map<HookType, List<MatchedHookStep>> hooksByType = entry.getValue().stream()
					.collect(Collectors.groupingBy(hookStep -> hookStep.getTestStep().getHookType()));
			hooksByType.entrySet().stream().sorted((e1, e2) -> e1.getKey().ordinal() - e2.getKey().ordinal()).map(e -> {
				try {
					return new HooksCodeMining(lineNumber, document, JavaReferencesCodeMiningProvider.this, e.getKey(),
							e.getValue(), javaProject);
				} catch (BadLocationException ble) {
					return null;
				}
			}).filter(Objects::nonNull).forEach(list::add);
		}
		return list;
	}

	@Override
	public void dispose() {

	}

	private static final class HooksCodeMining extends LineHeaderCodeMining {

		private HookType hookType;
		private List<MatchedHookStep> list;
		private AtomicReference<Consumer<MouseEvent>> action = new AtomicReference<>();
		private IJavaProject javaProject;

		public HooksCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider,
				HookType hookType, List<MatchedHookStep> list, IJavaProject javaProject) throws BadLocationException {
			super(beforeLineNumber, document, provider);
			this.hookType = hookType;
			this.list = list;
			this.javaProject = javaProject;
		}

		@Override
		protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
			return CompletableFuture.supplyAsync(() -> {
				String hookName;
				switch (hookType) {
				case BEFORE:
					hookName = "@Before";
					break;
				case AFTER:
					hookName = "@After";
					break;
				case AFTER_STEP:
					hookName = "@AfterStep";
					break;
				case BEFORE_STEP:
					hookName = "@BeforeStep";
					break;

				default:
					hookName = hookType.toString();
				}
				int size = list.size();
				setLabel(hookName + ": " + size);
				Set<Entry<MatchedHookStep, IMethod[]>> resolvedMethods = list.stream()
						.collect(Collectors.toMap(Function.identity(), step -> {
							try {
								return JDTUtil.resolveMethod(javaProject, step.getCodeLocation(), null);
							} catch (JavaModelException e) {
								return null;
							}
						})).entrySet();
				action.set(event -> {
					Shell shell = SWTUtil.getShell(event);
					if (resolvedMethods.size() == 1) {
						Entry<MatchedHookStep, IMethod[]> entry = resolvedMethods.iterator().next();
						open(entry.getKey(), entry.getValue(), shell);
					} else {
						ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider() {
							@Override
							public String getText(Object element) {
								if (element instanceof Entry<?, ?>) {
									Entry<?, ?> entry = (Entry<?, ?>) element;
									Object value = entry.getValue();
									if (value instanceof IMethod) {
										try {
											return JDTUtil.getMethodName((IMethod) value);
										} catch (JavaModelException e) {
										}
									}
									Object key = entry.getKey();
									if (key instanceof MatchedHookStep) {
										MatchedHookStep step = (MatchedHookStep) key;
										return step.getCodeLocation().toString();
									}
								}
								return element.toString();
							}
						});
						dialog.setElements(resolvedMethods.toArray());
						if (dialog.open() == Window.OK) {
							for (Object e : dialog.getResult()) {
								@SuppressWarnings("unchecked")
								Entry<MatchedHookStep, IMethod[]> entry = (Entry<MatchedHookStep, IMethod[]>) e;
								open(entry.getKey(), entry.getValue(), shell);
							}
						}
					}

					// TODO new ElementListSelectionDialog(null, null)
				});
				return null;
			});
		}

		private void open(MatchedHookStep step, IMethod[] method, Shell shell) {
			if (method != null && method.length > 0) {
				JavaStepDefinitionOpener.showMethod(method, shell);
				return;
			}
			MessageDialog.openInformation(shell, "Method not found",
					"Location " + step.getCodeLocation() + " not found");
		}

		@Override
		public Consumer<MouseEvent> getAction() {
			return action.get();
		}

	}

	private static final class JavaReferenceCodeMining extends LineContentCodeMining {

		private TestStep testStep;

		public JavaReferenceCodeMining(Position position, ICodeMiningProvider provider, TestStep testStep) {
			super(position, provider);
			this.testStep = testStep;
		}

		@Override
		protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
			return CompletableFuture.supplyAsync(() -> {
				// TODO special handling -> resolve to java ....
				setLabel(testStep.getCodeLocation());
				return null;
			});
		}

	}

	private static final Position createPosition(Location location, IDocument document) throws BadLocationException {
		int line = location.getLine();
		int offset = document.getLineOffset(line - 1);
		int lineLength = document.getLineLength(line - 1);
		return new Position(offset + lineLength - 1, 1);
	}

}
