package cucumber.eclipse.editor.steps;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getStepDefinitions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepDefinitionsRepository;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public class ExtensionRegistryStepProvider implements IStepProvider {

	public static final ExtensionRegistryStepProvider INSTANCE = new ExtensionRegistryStepProvider();
	
	private StepDefinitionsRepository stepDefinitionsRepository = StepDefinitionsRepository.INSTANCE;
	
	private List<IStepDefinitions> stepDefinitions = getStepDefinitions();
	
	private List<IStepListener> stepDefinitionsListeners = new CopyOnWriteArrayList<IStepListener>();

	private IFile file;
	
	private ExtensionRegistryStepProvider() {
	}
	
	public void addStepListener(IStepListener listener) {
		stepDefinitionsListeners.add(listener);
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.addStepListener(listener);
		}
	}
	
	public Set<Step> getStepsInEncompassingProject() {
		return this.stepDefinitionsRepository.getAllStepDefinitions();
	}
	
	public Set<IFile> getAllStepDefinitionsFile() {
		return this.stepDefinitionsRepository.getAllStepDefinitionsFiles();
	}
	
//	/**
//	 * Asyncrounous load steps 
//	 * @deprecated the builder do the job now
//	 */
//	public void reload(IJobChangeListener jobChangeListener) {
//		final Job job = new Job("Scanning for step definitions") {
//
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					getSteps(monitor);
//				} catch (CoreException e) {
//					return new Status(IStatus.ERROR, "cucumber.eclipse.editor", "reloading step definitions failed ("+e+")", e);
//					
//				} catch(OperationCanceledException oce) {
//					return Status.CANCEL_STATUS;
//				}
//				
//				if (monitor.isCanceled()) {
//					return Status.CANCEL_STATUS;
//				}
//				return Status.OK_STATUS;
//			}
//	     };
//	     if(jobChangeListener != null) {
//	    	 job.addJobChangeListener(jobChangeListener);
//	     }
//	     job.setUser(true);
//	     job.schedule();
//	}

	public Set<Step> findStepDefinitions(IResource resource, MarkerFactory markerFactory, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		boolean isFile = resource instanceof IFile;
		if(!isFile) {
			return new HashSet<Step>();
		}
		IFile stepDefinitionFile = (IFile) resource;
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Scan steps definitions for " + resource.getName(), stepDefinitions.size());

		int stepDefinitionsCounter = 0;
		for (IStepDefinitions stepDefinitionsService : stepDefinitions) {
			Set<Step> stepDefs = stepDefinitionsService.findStepDefintions(stepDefinitionFile, markerFactory, subMonitor);
			if(!stepDefs.isEmpty()) {
				stepDefinitionsCounter += stepDefs.size();
	//			System.out.println(stepDefinitionsService.supportedLanguage() + " found " + stepDefs.size() + " step definitions in " + stepDefinitionFile.getName());
				this.stepDefinitionsRepository.add(stepDefinitionFile, stepDefs);
			}
		}
		long end = System.currentTimeMillis();
		long duration = end - start;
		if(stepDefinitionsCounter > 0 || duration > 0) {
			System.out.println("findStepDefs (" + resource.getName() + ") return " + stepDefinitionsCounter + " step definitions in " + (duration) + "ms.");
		}
		return this.stepDefinitionsRepository.getAllStepDefinitions();
	}
	
	public Set<Step> getSteps(IProgressMonitor progressMonitor) throws CoreException {
		long start = System.currentTimeMillis();
		Set<Step> newSteps = new LinkedHashSet<Step>();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, "Reloading steps", stepDefinitions.size());
		Map<String, Long> metrics = new HashMap<String, Long>();  
		try {
			for (IStepDefinitions stepDef : stepDefinitions) {
				long startStepDefinitionScan = System.currentTimeMillis();
				newSteps.addAll(stepDef.getSteps(file, subMonitor.newChild(1)));
				long endStepDefinitionScan = System.currentTimeMillis();
				metrics.put(stepDef.supportedLanguage(), endStepDefinitionScan - startStepDefinitionScan);
				if (subMonitor.isCanceled()) {
					return Collections.emptySet();
				}
			}
		} finally {
			 if (progressMonitor != null) {
				 progressMonitor.done();
	         }
			 long end = System.currentTimeMillis();
			 StringBuffer stringBuffer = new StringBuffer();
			 for (Entry<String, Long> metric : metrics.entrySet()) {
				stringBuffer.append(metric.getKey()).append("=").append(metric.getValue()).append(" ");
			 }
			 System.out.println("ExtensionRegistryStepProvider scans step definitions in " + (end - start) + " ms. " + stringBuffer.toString());
		}
		return newSteps;
	}

	public void removeStepListener(IStepListener listener) {
		stepDefinitionsListeners.remove(listener);
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.removeStepListener(listener);
		}
	}

	public void clean() {
		this.stepDefinitionsRepository.reset();
		GlueRepository.INSTANCE.clean();
	}
}
