package io.cucumber.eclipse.editor.codemining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseEvent;

import io.cucumber.eclipse.editor.CucumberServiceRegistry;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.editor.launching.Mode;
import io.cucumber.eclipse.editor.preferences.CucumberEditorPreferences;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;
import io.cucumber.tagexpressions.TagExpressionParser;

/**
 * Supplies "run" tags as codeminings if available
 * 
 * @author christoph
 *
 */
public class CucumberRunCodeMiningProvider implements ICodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			IDocument document = viewer.getDocument();

			GherkinEditorDocument editorDocument = GherkinEditorDocumentManager.get(document);
			if (editorDocument == null) {
				return Collections.emptyList();
			}
			List<ICodeMining> list = new ArrayList<>();
			CucumberEditorPreferences preferences = CucumberEditorPreferences.of(editorDocument.getResource());
			for (Mode mode : Mode.values()) {
				if (!preferences.isShowShortcutFor(mode)) {
					continue;
				}
				for (ILauncher launcher : CucumberServiceRegistry.getLauncher()) {
					if (launcher.supports(editorDocument.getResource())) {
						if (launcher.supports(mode)) {
							runnable(editorDocument.getScenarios(), Scenario::getLocation, editorDocument, launcher,
									mode).forEach(list::add);
							runnable(editorDocument.getFeature().stream(), Feature::getLocation, editorDocument,
									launcher, mode).forEach(list::add);
							runnable(editorDocument.getTags(), Tag::getLocation, editorDocument, launcher, mode)
									.forEach(list::add);
						}
					}
				}
			}
			return list;
		});
	}

	private <T> Stream<RunnableElementCodeMining> runnable(Stream<T> stream, Function<T, Location> locationProvider,
			GherkinEditorDocument document, ILauncher launcher, Mode mode) {
		return stream.map(runnable -> {
			Location location = locationProvider.apply(runnable);
			try {
				Position position = document.getEolPosition(location);
				return new RunnableElementCodeMining(position, runnable, launcher, mode,
						CucumberRunCodeMiningProvider.this);
			} catch (BadLocationException e) {
				return null;
			}
		}).filter(Objects::nonNull);

	}

	@Override
	public void dispose() {

	}

	private static final class RunnableElementCodeMining extends LineContentCodeMining {

		AtomicReference<Consumer<MouseEvent>> action = new AtomicReference<>();

		private Object element;

		private Mode mode;

		private ILauncher launcher;

		public RunnableElementCodeMining(Position position, Object element, ILauncher launcher, Mode mode,
				ICodeMiningProvider provider) throws BadLocationException {
			super(position, provider);
			this.element = element;
			this.launcher = launcher;
			this.mode = mode;
		}

		@Override
		protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
			return CompletableFuture.runAsync(() -> {
				// workaround for bug Bug 570774
				setLabel((mode.ordinal() == 0 ? " " : "") + mode.getSymbol() + " " + mode.toString());
				action.set(event -> {
					Job.create("Launching Cucumber", new ICoreRunnable() {

						@Override
						public void run(IProgressMonitor monitor) throws CoreException {
							launcher.launch(GherkinEditorDocumentManager.get(viewer.getDocument()),
									new StructuredSelection(getElement()), mode, !(element instanceof Feature),
									monitor);

						}
					}).schedule();
				});
			});
		}

		private Object getElement() {
			if (element instanceof Tag) {
				String name = ((Tag) element).getName();
				return TagExpressionParser.parse(name);
			}
			return element;
		}

		@Override
		public Consumer<MouseEvent> getAction() {
			return action.get();
		}
	}

}
