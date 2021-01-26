package io.cucumber.eclipse.editor.contentassist;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

import io.cucumber.cucumberexpressions.CucumberExpressionParserSupport;
import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.CucumberServiceRegistry;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinKeyword;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.steps.StepDefinition;

/**
 * Provides content assists for cucumber steps
 * 
 * @author christoph
 *
 */
public class CucumberContentAssistProcessor implements IContentAssistProcessor {

	private static final TemplateContextType CONTEXT_TYPE = new TemplateContextType(
			CucumberContentAssistProcessor.class.getName(), "Cucumber");

	private ConcurrentMap<IProject, StepDefSearchJob> jobMap = new ConcurrentHashMap<>();

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();

		LinkedModeModel model = LinkedModeModel.getModel(document, offset);
		if (model != null && model.anyPositionContains(offset)) {
			return null;
		}
		GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
		try {
			IRegion line = viewer.getDocument().getLineInformationOfOffset(offset);

			String currentLine = viewer.getDocument().get(line.getOffset(), offset - line.getOffset());
			String typed = currentLine.stripLeading();
			int stripped = (currentLine.length() - typed.length());

			Optional<GherkinKeyword> keywordPrefix = editorDocument.getStepElementKeywords()
					.sorted(Collections.reverseOrder((s1, s2) -> s1.getKey().length() - s2.getKey().length()))
					.filter(keyWord -> typed.startsWith(keyWord.getKey() + " ")).findFirst();
			if (keywordPrefix.isPresent()) {
				IResource resource = editorDocument.getResource();
				if (resource != null) {
					IProject project = resource.getProject();
					StepDefSearchJob job = jobMap.compute(project, (p, j) -> {
						if (j == null) {
							j = new StepDefSearchJob(p);
							j.schedule();
						} else {
							if (j.definitions != null) {
								j.definitions = null;
								// update existing definitions
								j.schedule();
							}
						}
						return j;
					});
					try {
						job.join();
						Collection<StepDefinition> steps = job.definitions;
						if (steps != null) {
							String keyWord = keywordPrefix.get().getKey();
							int keyWordLength = keyWord.length() + 1 + stripped;
							int keyWordOffset = line.getOffset() + keyWordLength;
							IRegion region = new Region(keyWordOffset, line.getLength() - keyWordLength);
							CucumberDocumentTemplateContext ctx = new CucumberDocumentTemplateContext(
									viewer.getDocument(), region);
							Image icon = Activator.getDefault().getImageRegistry().get(Activator.ICON_CUKES);
							ICompletionProposal[] proposals = steps.stream().map(s -> {
								Template template = CucumberExpressionParserSupport.createTemplate(s,
										CONTEXT_TYPE.getId());
								return new TemplateProposal(template, ctx, region, icon);
							}).toArray(ICompletionProposal[]::new);
							return proposals;
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

				}
			}
		} catch (BadLocationException e) {
			Activator.getDefault().getLog().warn("Invalid location encountered while computing context information", e);
		}
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	private static final class StepDefSearchJob extends Job {

		private final IProject project;

		private volatile Collection<StepDefinition> definitions;

		public StepDefSearchJob(IProject project) {
			super("Compute Step definitions");
			this.project = project;
			setUser(false);
			setPriority(Job.BUILD);
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			List<IStepDefinitionsProvider> providers = CucumberServiceRegistry.getStepDefinitionsProvider(project);
			definitions = providers.stream().flatMap(provider -> {
				try {
					return provider.findStepDefinitions(project, monitor).stream();
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(e.getStatus());
					return Stream.empty();
				}
			}).collect(Collectors.toList());
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

	private static final class CucumberDocumentTemplateContext extends DocumentTemplateContext {

		public CucumberDocumentTemplateContext(IDocument document, IRegion region) {
			super(CONTEXT_TYPE, document, region.getOffset(), region.getLength());
		}

		@Override
		public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
			TemplateBuffer buffer = CucumberExpressionParserSupport.evaluate(template);
			getContextType().resolve(buffer, this);
			return buffer;
		}

	}

}
