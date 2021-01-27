package io.cucumber.eclipse.editor.contentassist;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
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
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import io.cucumber.cucumberexpressions.CucumberExpressionParserSupport;
import io.cucumber.eclipse.editor.Activator;
import io.cucumber.eclipse.editor.CucumberServiceRegistry;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinKeyword;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.steps.StepDefinition;

/**
 * Bridging cucumber glue step definitions to the eclipse template framework
 * 
 * @author christoph
 *
 */
public class CucumberTemplates {

	private static final TemplateContextType CONTEXT_TYPE = new TemplateContextType(
			CucumberStepContentAssistProcessor.class.getName(), "Cucumber");

	private static final ConcurrentMap<IProject, StepDefSearchJob> jobMap = new ConcurrentHashMap<>();

	private static final Comparator<CucumberTemplateProposal> DISPLAYSTRING_ORDER = (t1, t2) -> {
		return t1.getDisplayString().compareToIgnoreCase(t2.getDisplayString());
	};

	private static final Comparator<CucumberTemplateProposal> RELEVANCE_ORDER = (t1, t2) -> {
		int cmp = t2.getRelevance() - t1.getRelevance();
		if (cmp == 0) {
			return DISPLAYSTRING_ORDER.compare(t1, t2);
		}
		return cmp;
	};

	/**
	 * Computes the set of template completion proposals relevant for the given
	 * viewer and offset
	 * 
	 * @param viewer     the viewer to ask for the document
	 * @param offset     the offset the proposals are supposed to be computed
	 * @param stepFilter intercepter that can modify found proposals or reject them
	 *                   completely by returning false
	 * @return the list of proposals in relevance order
	 */
	public static ICompletionProposal[] computeTemplateProposals(ITextViewer viewer, int offset,
			Predicate<CucumberStepProposal> stepFilter) {
		IDocument document = viewer.getDocument();

		LinkedModeModel model = LinkedModeModel.getModel(document, offset);
		if (model != null && model.anyPositionContains(offset)) {
			return null;
		}
		GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
		if (editorDocument == null) {
			return null;
		}
		try {
			IRegion line = document.getLineInformationOfOffset(offset);
			String currentLine = viewer.getDocument().get(line.getOffset(), offset - line.getOffset());
			String typed = currentLine.stripLeading();
			int stripped = (currentLine.length() - typed.length());

			Optional<GherkinKeyword> keywordPrefix = editorDocument.getKeyWordOfLine(typed);
			if (keywordPrefix.isPresent()) {
				IResource resource = editorDocument.getResource();
				if (resource != null) {
					IProject project = resource.getProject();
					StepDefSearchJob job = jobMap.compute(project, (p, j) -> {
						if (j == null) {
							j = new StepDefSearchJob(p, viewer, offset);
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
							GherkinKeyword gherkinKeyword = keywordPrefix.get();
							String keyWord = gherkinKeyword.getKey();
							int keyWordLength = keyWord.length() + 1 + stripped;
							int keyWordOffset = line.getOffset() + keyWordLength;
							String fullLine = document.get(line.getOffset(), line.getLength()).stripLeading();
							String text = fullLine.substring(keyWord.length() + 1);
							String prefix = typed.substring(keyWord.length() + 1);
							IRegion region = new Region(keyWordOffset, line.getLength() - keyWordLength);
							CucumberDocumentTemplateContext ctx = new CucumberDocumentTemplateContext(
									viewer.getDocument(), region);
							Image icon = Activator.getDefault().getImageRegistry().get(Activator.ICON_CUKES);
							ICompletionProposal[] proposals = steps.parallelStream()
									.map(stepDefinition -> new CucumberStepProposal(stepDefinition, gherkinKeyword,
											text, prefix))
									.filter(stepProposal -> stepFilter.test(stepProposal)).map(stepProposal -> {
										return new CucumberTemplateProposal(stepProposal.getTemplate(), ctx, region,
												icon, stepProposal.relevance,
												stepProposal.getStepDefinition().getDescription());
									}).sorted(RELEVANCE_ORDER).toArray(ICompletionProposal[]::new);
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

	private static final class CucumberTemplateProposal extends TemplateProposal {

		private String description;

		public CucumberTemplateProposal(Template template, TemplateContext context, IRegion region, Image image,
				int relevance, String description) {
			super(template, context, region, image, relevance);
			this.description = description;
			if (description != null && description.startsWith("<html")) {
				setInformationControlCreator(new IInformationControlCreator() {

					@Override
					public IInformationControl createInformationControl(Shell parent) {
						return new HtmlInformationControl(parent, description);
					}
				});
			}
		}

		@Override
		public String getAdditionalProposalInfo() {
			if (description != null) {
				return description;
			}
			return getTemplate().getDescription();
		}

		@Override
		public String getDisplayString() {
			return getTemplate().getPattern();
		}

	}

	private static final class StepDefSearchJob extends Job {

		private final IProject project;

		private volatile Collection<StepDefinition> definitions;

		private ITextViewer viewer;

		private int offset;

		public StepDefSearchJob(IProject project, ITextViewer viewer, int offset) {
			super("Compute Step definitions");
			this.project = project;
			this.viewer = viewer;
			this.offset = offset;
			setUser(false);
			setPriority(Job.BUILD);
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			List<IStepDefinitionsProvider> providers = CucumberServiceRegistry.getStepDefinitionsProvider(project);
			definitions = providers.stream().flatMap(provider -> {
				try {
					return provider.findStepDefinitions(viewer, offset, project, monitor).stream();
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(e.getStatus());
					return Stream.empty();
				}
			}).collect(Collectors.toList());
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

	public static class CucumberStepProposal {

		private StepDefinition stepDefinition;
		private GherkinKeyword gherkinKeyword;
		private String text;
		private String typed;
		private Template template;
		private int relevance;

		private CucumberStepProposal(StepDefinition stepDefinition, GherkinKeyword gherkinKeyword, String text,
				String typed) {
			this.stepDefinition = stepDefinition;
			this.gherkinKeyword = gherkinKeyword;
			this.text = text;
			this.typed = typed;
		}

		public Template getTemplate() {
			if (template == null) {
				template = CucumberExpressionParserSupport.createTemplate(stepDefinition, CONTEXT_TYPE.getId());
			}
			return template;
		}

		/**
		 * Set the relevance of this proposal, higher is better, the default relevance
		 * is 0.
		 * 
		 * @param relevance the new relevance
		 */
		public void setRelevance(int relevance) {
			this.relevance = relevance;
		}

		/**
		 * 
		 * @return the current line text without the keyword
		 */
		public String getLineText() {
			return text;
		}

		/**
		 * 
		 * @return the keyword for this line
		 */
		public GherkinKeyword getGherkinKeyword() {
			return gherkinKeyword;
		}

		/**
		 * @return the prefix text where the current cursor position is located
		 */
		public String getLinePrefix() {
			return typed;
		}

		/**
		 * 
		 * @return the step definition
		 */
		public StepDefinition getStepDefinition() {
			return stepDefinition;
		}

	}

}
