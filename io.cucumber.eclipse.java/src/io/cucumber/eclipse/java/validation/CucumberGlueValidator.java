package io.cucumber.eclipse.java.validation;

import static io.cucumber.eclipse.editor.Tracing.PERFORMANCE_STEPS;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.marker.MarkerFactory;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberMatchedStepsPlugin;
import io.cucumber.eclipse.java.plugins.CucumberMissingStepsPlugin;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.plugins.CucumberStepParserPlugin;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.eclipse.java.steps.StepGenerator;

/**
 * Performs a dry-run on the document to verify step definition matching
 * 
 * @author christoph
 *
 */
public class CucumberGlueValidator implements IDocumentSetupParticipant {

	private static ConcurrentMap<IDocument, GlueJob> jobMap = new ConcurrentHashMap<>();

	static {
		// TODO implement generic DocumentCache class
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new IFileBufferListener() {

			@Override
			public void underlyingFileMoved(IFileBuffer buffer, IPath path) {

			}

			@Override
			public void underlyingFileDeleted(IFileBuffer buffer) {

			}

			@Override
			public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {

			}

			@Override
			public void stateChanging(IFileBuffer buffer) {

			}

			@Override
			public void stateChangeFailed(IFileBuffer buffer) {

			}

			@Override
			public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
				System.out.println("dirtyStateChanged: " + buffer.getLocation() + " dirty = " + isDirty);

			}

			@Override
			public void bufferDisposed(IFileBuffer buffer) {
				if (buffer instanceof ITextFileBuffer) {
					IDocument document = ((ITextFileBuffer) buffer).getDocument();
					jobMap.remove(document);
				}

			}

			@Override
			public void bufferCreated(IFileBuffer buffer) {

			}

			@Override
			public void bufferContentReplaced(IFileBuffer buffer) {

			}

			@Override
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {

			}
		});
	}

	@Override
	public void setup(IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				// TODO configurable
				validate(document, 1000, false);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {

			}
		});
		validate(document, 0, false);
	}

	private void validate(IDocument document, int delay, boolean persistent) {
		jobMap.compute(document, (key, oldJob) -> {
			if (oldJob != null && !oldJob.persistent) {
				oldJob.cancel();
			}
			GlueJob verificationJob = new GlueJob(oldJob, document, persistent);
			verificationJob.setUser(false);
			verificationJob.setPriority(Job.DECORATE);
			if (delay > 0) {
				verificationJob.schedule(delay);
			} else {
				verificationJob.schedule();
			}
			return verificationJob;
		});

	}

	/**
	 * Allows to sync with the current glue code computation
	 * 
	 * @param document the document to sync on
	 * @param monitor  the progress monitor that can be used to cancel the join
	 *                 operation, or null if cancellation is not required. No
	 *                 progress is reported on this monitor.
	 * @throws OperationCanceledException on cancellation
	 * @throws InterruptedException       if the thread was interrupted while
	 *                                    waiting
	 */
	private static GlueJob sync(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		GlueJob glueJob = jobMap.get(document);
		if (glueJob != null) {
			glueJob.join(TimeUnit.SECONDS.toMillis(30), monitor);
		}
		return glueJob;
	}

	public static Collection<MatchedStep<?>> getMatchedSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		GlueJob job = sync(document, monitor);
		if (job != null) {
			return job.matchedSteps;
		}
		return Collections.emptyList();
	}

	public static Collection<CucumberStepDefinition> getAvaiableSteps(IDocument document, IProgressMonitor monitor)
			throws OperationCanceledException, InterruptedException {
		GlueJob job = sync(document, monitor);
		if (job != null) {
			return job.parsedSteps;
		}
		return Collections.emptyList();
	}

	private static final class GlueJob extends Job {

		private GlueJob oldJob;
		private IDocument document;
		private boolean persistent;

		transient Collection<MatchedStep<?>> matchedSteps;
		transient Collection<CucumberStepDefinition> parsedSteps;

		public GlueJob(GlueJob oldJob, IDocument document, boolean persistent) {
			super("Verify Cucumber Glue Code");
			this.oldJob = oldJob;
			this.document = document;
			this.persistent = persistent;
			if (oldJob != null) {
				this.matchedSteps = oldJob.matchedSteps;
				this.parsedSteps = oldJob.parsedSteps;
			} else {
				this.matchedSteps = Collections.emptySet();
				this.parsedSteps = Collections.emptySet();
			}
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (oldJob != null) {
				try {
					oldJob.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}
			}
			GherkinEditorDocument editorDocument = GherkinEditorDocument.get(document);
			if (editorDocument != null) {
				try {
					IResource resource = editorDocument.getResource();
					IJavaProject javaProject = JDTUtil.getJavaProject(resource);
					if (javaProject != null) {
						long start = System.currentTimeMillis();
						DebugTrace debug = Tracing.get();
						debug.traceEntry(PERFORMANCE_STEPS, resource);
						try (CucumberRuntime rt = CucumberRuntime.create(javaProject)) {
							rt.getRuntimeOptions().setDryRun();
							try {
								rt.addFeature(editorDocument);
							} catch (FeatureParserException e) {
								// the feature has syntax errors, we can't check the glue then...
								return Status.CANCEL_STATUS;
							}
							CucumberMissingStepsPlugin missingStepsPlugin = new CucumberMissingStepsPlugin();
							CucumberStepParserPlugin stepParserPlugin = new CucumberStepParserPlugin();
							CucumberMatchedStepsPlugin matchedStepsPlugin = new CucumberMatchedStepsPlugin();
							rt.addPlugin(stepParserPlugin);
							rt.addPlugin(matchedStepsPlugin);
							rt.addPlugin(missingStepsPlugin);
							try {
								rt.run(monitor);
								Map<Integer, Collection<String>> snippets = missingStepsPlugin.getSnippets();
								MarkerFactory.missingSteps(resource, snippets, StepGenerator.class.getName(),
										persistent);
								Collection<CucumberStepDefinition> steps = stepParserPlugin.getStepList();
								matchedSteps = Collections.unmodifiableCollection(matchedStepsPlugin.getMatchedSteps());
								parsedSteps = Collections.unmodifiableCollection(stepParserPlugin.getStepList());
								debug.traceExit(PERFORMANCE_STEPS,
										matchedSteps.size() + " step(s) /  " + steps.size() + " step(s)  matched, "
												+ snippets.size() + " snippet(s) where suggested || total run time "
												+ (System.currentTimeMillis() - start) + "ms)");

							} catch (Throwable e) {
								// TODO
								System.out.println(e);
							}
						}
					}
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
//			jobMap.remove(document, this);
			// FIXME notify document reconsilers??
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

}
