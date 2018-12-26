package cucumber.eclipse.editor.builder;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.steps.UniversalStepDefinitionsProvider;
import cucumber.eclipse.steps.integration.StepPreferences;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

/**
 * Incremental builder of step definitions.
 * 
 * This is a generic builder independent of the language used to create the step
 * definitions.
 * 
 * The step definitions detection is delegated to IStepDefinition provides by
 * external plug-ins.
 * 
 * This builder DOES NOT MAINTAIN glue, see {#CucumberGherkinBuilder} for this
 * job.
 * 
 * @author qvdk
 *
 */
public class CucumberStepDefinitionsBuilder extends IncrementalProjectBuilder {

	public static final String ID = "cucumber.eclipse.builder.stepdefinition";

	private final UniversalStepDefinitionsProvider stepDefinitionsProvider = UniversalStepDefinitionsProvider.INSTANCE;
	private MarkerFactory markerFactory = MarkerFactory.INSTANCE;
	private StepPreferences cucumberPreferences = StepPreferences.INSTANCE;
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		if (!cucumberPreferences.isStepDefinitionsMatchingEnabled()) {
			return null;
		}
		switch (kind) {
		case FULL_BUILD:
			System.out.println("step definitions full build");
			fullBuild(markerFactory, monitor);
			break;
		case AUTO_BUILD:
		case INCREMENTAL_BUILD:
			IResourceDelta delta = getDelta(getProject());
			System.out.println("step definitions incremental build on " + delta.getResource().getName());
			incrementalBuild(delta, markerFactory, monitor);
			break;
		case CLEAN_BUILD:
			System.out.println("clean build");
			break;
		default:
			break;
		}

		return null;
	}

	protected void fullBuild(MarkerFactory markerFactory, final IProgressMonitor monitor) throws CoreException {
		try {
			IProject project = getProject();
			stepDefinitionsProvider.clean(project);
			project.accept(new CucumberStepDefinitionsBuildVisitor(markerFactory, monitor));
			stepDefinitionsProvider.persist(project, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	protected void incrementalBuild(IResourceDelta delta, MarkerFactory markerFactory, IProgressMonitor monitor)
			throws CoreException {
		try {
			IProject project = delta.getResource().getProject();
			if(!stepDefinitionsProvider.isInitialized(project)) {
				stepDefinitionsProvider.load(project);
			}
			// the visitor does the work.
			delta.accept(new CucumberStepDefinitionsBuildVisitor(markerFactory, monitor));
			stepDefinitionsProvider.persist(getProject(), monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	class CucumberStepDefinitionsBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {

		private IProgressMonitor monitor;
		private MarkerFactory markerFactory;

		public CucumberStepDefinitionsBuildVisitor(MarkerFactory markerFactory, IProgressMonitor monitor) {
			this.monitor = monitor;
			this.markerFactory = markerFactory;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			// System.out.println("CucumberFullBuildVisitor is building " +
			// resource.getName());
			// build the specified resource.
			// return true to continue visiting children.
			
			if(monitor.isCanceled()) {
				return false;
			}
			
			if(!resource.exists()) {
				// skip 
				return true;
			}
			if(stepDefinitionsProvider.support(resource)) {
				this.markerFactory.cleanMarkers(resource);
				stepDefinitionsProvider.findStepDefinitions(resource, markerFactory, monitor);
			}
			
			return true;
		}
		
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			// stop the visitor pattern if a cancellation was requested
			if(monitor.isCanceled()) {
				return false;
			}
						
			IResource resource = delta.getResource();
//			System.out.println("CucumberIncrementalBuildVisitor is building " + delta.getResource().getName());

			return visit(resource);
		}

	}

	
	class CucumberStepDefinitionsCleanBuildVisitor implements IResourceVisitor {

		private IProgressMonitor monitor;
		private MarkerFactory markerFactory;

		public CucumberStepDefinitionsCleanBuildVisitor(MarkerFactory markerFactory, IProgressMonitor monitor) {
			this.monitor = monitor;
			this.markerFactory = markerFactory;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			// stop the visitor pattern if a cancellation was requested
			if(monitor.isCanceled()) {
				return false;
			}
			this.markerFactory.cleanMarkers(resource);
			return true;
		}

	}
	

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// stop the visitor pattern if a cancellation was requested
		System.out.println("CucumberStepDefinitionsBuilder.clean");
		IProject project = getProject();
		this.stepDefinitionsProvider.clean(project);
		project.accept(new CucumberStepDefinitionsCleanBuildVisitor(markerFactory, monitor));
	}
}
