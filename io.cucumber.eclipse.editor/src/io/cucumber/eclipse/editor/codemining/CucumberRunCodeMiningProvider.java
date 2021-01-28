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

import org.eclipse.core.runtime.IProgressMonitor;
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
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.editor.launching.ILauncher.Mode;
import io.cucumber.eclipse.editor.launching.LaunchTag;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Tag;
import io.cucumber.messages.Messages.Location;

/**
 * Supplies "run" tags as codeminings if available
 * 
 * @author christoph
 *
 */
//TODO make configurable....
public class CucumberRunCodeMiningProvider implements ICodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			IDocument document = viewer.getDocument();

			GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
			if (editorDocument == null) {
				return Collections.emptyList();
			}
			List<ICodeMining> list = new ArrayList<>();
			for (ILauncher launcher : CucumberServiceRegistry.getLauncher()) {
				if (launcher.supports(editorDocument.getResource())) {
					for (Mode mode : ILauncher.Mode.values()) {
						if (launcher.supports(mode)) {
							runnable(editorDocument.getScenarios(), Scenario::getLocation, editorDocument, launcher,
									mode)
									.forEach(list::add);
							runnable(editorDocument.getFeature().stream(), Feature::getLocation, editorDocument,
									launcher, mode)
									.forEach(list::add);
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
				System.out.println("runnable " + runnable.getClass().getSimpleName() + " at " + location.getLine()
						+ "::" + location.getColumn());
				return new RunnableElementCodeMining(document.getPosition(location), runnable, launcher, mode,
						CucumberRunCodeMiningProvider.this);
			} catch (BadLocationException e) {
				System.err.println(e);
				return null;
			}
		}).filter(Objects::nonNull);

	}

	@Override
	public void dispose() {

	}

	private static final class RunnableElementCodeMining extends LineContentCodeMining {

		private static final String PREFIX = " " + (char) 0x25B7 + " ";

		AtomicReference<Consumer<MouseEvent>> action = new AtomicReference<>();

		private Object element;

		private Mode mode;

		private ILauncher launcher;

		public RunnableElementCodeMining(Position position, Object element, ILauncher launcher, Mode mode,
				ICodeMiningProvider provider)
				throws BadLocationException {
			super(position, provider);
			this.element = element;
			this.launcher = launcher;
			this.mode = mode;
		}

		@Override
		protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
			return CompletableFuture.runAsync(() -> {
				setLabel(PREFIX + mode + " ");
				action.set(event -> {
					launcher.launch(GherkinEditorDocument.get(viewer.getDocument()),
							new StructuredSelection(getElement()),
							mode);
				});
			});
		}


		private Object getElement() {
			if (element instanceof Tag) {
				return new LaunchTag(((Tag) element).getName(), true);
			}
			return element;
		}

		@Override
		public Consumer<MouseEvent> getAction() {
			return action.get();
		}
	}

}
