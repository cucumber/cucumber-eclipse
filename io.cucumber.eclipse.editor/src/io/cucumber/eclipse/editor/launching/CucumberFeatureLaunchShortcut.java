package io.cucumber.eclipse.editor.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import io.cucumber.eclipse.editor.CucumberServiceRegistry;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.launching.ILauncher.Mode;
import io.cucumber.messages.Messages.GherkinDocument.Feature;

public class CucumberFeatureLaunchShortcut implements ILaunchShortcut {

	private static final ILauncher NO_LAUNCHER = new ILauncher() {
		@Override
		public void launch(Map<GherkinEditorDocument, IStructuredSelection> selection, Mode mode,
				boolean temporary, IProgressMonitor monitor) {
			// TODO inform the user about unable to launch
		}

		@Override
		public boolean supports(IResource resource) {
			return true;
		}

		@Override
		public boolean supports(Mode mode) {
			return true;
		}
	};

//	private String newLaunchConfigurationName;

	@Override
	public void launch(IEditorPart editor, String mode) {
		Mode modeType = Mode.parseString(mode);
		if (modeType == null) {
			// unsupported....
			return;
		}
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			IEditorInput editorInput = textEditor.getEditorInput();
			IDocument document = textEditor.getDocumentProvider().getDocument(editorInput);
			ISelection selection = textEditor.getSelectionProvider().getSelection();
			if (document != null) {
				Job.create("Launching Cucumber", new ICoreRunnable() {

					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
						Optional<Feature> feature = editorDocument.getFeature();
						if (feature.isPresent()) {
							ILauncher launcher = getLauncher(modeType)
									.filter(l -> l.supports(editorDocument.getResource())).findAny()
									.orElse(NO_LAUNCHER);
							IStructuredSelection selected;
							if (!selection.isEmpty() && selection instanceof ITextSelection) {
								ITextSelection textSelection = (ITextSelection) selection;
								int startLine = textSelection.getStartLine() + 1;
								int endLine = textSelection.getEndLine() + 1;
								List<Object> selectedItems = new ArrayList<>();
								editorDocument.getTags()
										.filter(tag -> tag.getLocation().getLine() >= startLine
												&& tag.getLocation().getLine() <= endLine)
										.map(tag -> new LaunchTag(tag.getName(), true)).forEach(selectedItems::add);
								editorDocument.getScenarios()
										.filter(senario -> senario.getLocation().getLine() >= startLine
												&& senario.getLocation().getLine() <= endLine)
										.forEach(selectedItems::add);
								if (selectedItems.isEmpty()) {
									selectedItems.add(feature.get());
								}
								selected = new StructuredSelection(selectedItems);
							} else {
								selected = new StructuredSelection(feature.get());
							}
							launcher.launch(editorDocument, selected, modeType, true, monitor);
						} else {
							// TODO show error to the user
						}
					}
				}).schedule();
			}
		}
	}

	@Override
	public void launch(ISelection selection, String mode) {
		Mode modeType = Mode.parseString(mode);
		if (modeType == null) {
			// unsupported....
			return;
		}
		Job.create("Launching Cucumber", new ICoreRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				if (selection instanceof StructuredSelection) {
					Map<GherkinEditorDocument, StructuredSelection> documents = Arrays
							.stream(((StructuredSelection) selection).toArray())
							.map(o -> Adapters.adapt(o, IFile.class)).filter(Objects::nonNull)
							.map(GherkinEditorDocument::get).filter(doc -> doc.getFeature().isPresent())
							.collect(Collectors.toMap(Function.identity(),
									doc -> new StructuredSelection(doc.getFeature().get())));
					if (documents.isEmpty()) {
						// TODO inform the user, no launchabel documents!
					}
					List<ILauncher> launcher = getLauncher(modeType).collect(Collectors.toList());
					Map<ILauncher, List<Entry<GherkinEditorDocument, StructuredSelection>>> launchMap = documents
							.entrySet().stream()
							.collect(Collectors.groupingBy(
									entry -> launcher.stream().filter(l -> l.supports(entry.getKey().getResource()))
											.findAny().orElse(NO_LAUNCHER)));
					SubMonitor subMonitor = SubMonitor.convert(monitor, launchMap.size() * 100);
					for (Entry<ILauncher, List<Entry<GherkinEditorDocument, StructuredSelection>>> entry : launchMap
							.entrySet()) {
						entry.getKey().launch(
								entry.getValue().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)),
								modeType, false, subMonitor.split(100));
					}

				}
			}
		}).schedule();

	}

	private static Stream<ILauncher> getLauncher(Mode modeType) {
		return CucumberServiceRegistry.getLauncher().stream().filter(l -> l.supports(modeType));
	}

}
