package io.cucumber.eclipse.java.codemining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.validation.CucumberGlueValidator;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestStep;

/**
 * Provide java specific code minings to the editor
 * 
 * @author christoph
 *
 */
public class JavaReferencesCodeMiningProvider implements ICodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				IDocument document = viewer.getDocument();
				Collection<MatchedStep> steps = CucumberGlueValidator.getMatchedSteps(document, monitor);
				List<ICodeMining> list = new ArrayList<>();

				Map<Integer, List<MatchedStep>> stepByLine = steps.stream()
						.filter(step -> step.getTestStep() instanceof HookTestStep)
						.collect(Collectors.groupingBy(step -> step.getLocation().getLine()));
				for (Entry<Integer, List<MatchedStep>> entry : stepByLine.entrySet()) {
					int lineNumber = entry.getKey() - 1;
					Map<HookType, List<HookTestStep>> hooksByType = entry.getValue().stream()
							.map(MatchedStep::getTestStep).map(HookTestStep.class::cast)
							.collect(Collectors.groupingBy(hookStep -> hookStep.getHookType()));
					hooksByType.entrySet().stream().sorted((e1, e2) -> e1.getKey().ordinal() - e2.getKey().ordinal())
							.map(e -> {
								try {
									return new HooksCodeMining(lineNumber, document,
											JavaReferencesCodeMiningProvider.this, e.getKey(), e.getValue());
								} catch (BadLocationException e3) {
									return null;
								}
							})
							.filter(Objects::nonNull).forEach(list::add);
				}

				return list;
			} catch (OperationCanceledException e) {
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return Collections.emptyList();
		});
	}

	@Override
	public void dispose() {

	}

	private static final class HooksCodeMining extends LineHeaderCodeMining {

		private HookType hookType;
		private List<HookTestStep> list;

		public HooksCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider,
				HookType hookType, List<HookTestStep> list)
				throws BadLocationException {
			super(beforeLineNumber, document, provider);
			this.hookType = hookType;
			this.list = list;
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
				return null;
			});
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
