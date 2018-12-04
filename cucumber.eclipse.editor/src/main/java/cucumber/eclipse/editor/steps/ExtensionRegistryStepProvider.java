package cucumber.eclipse.editor.steps;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getStepDefinitions;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
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

public class ExtensionRegistryStepProvider implements IStepProvider {

	private AtomicReference<Set<Step>> steps = new AtomicReference<Set<Step>>(Collections.<Step>emptySet());

	private List<IStepDefinitions> stepDefinitions = getStepDefinitions();
	
	private List<IStepListener> stepDefinitionsListeners = new CopyOnWriteArrayList<IStepListener>();

	private IFile file;
	
	
	public ExtensionRegistryStepProvider(IFile file) {
		this.file = file;
	}

	public void addStepListener(IStepListener listener) {
		stepDefinitionsListeners.add(listener);
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.addStepListener(listener);
		}
	}

	public Set<Step> getStepsInEncompassingProject() {
		return steps.get();
	}
	
	/**
	 * Asyncrounous load steps 
	 */
	public void reload(IJobChangeListener jobChangeListener) {
		final Job job = new Job("Scanning for step definitions") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try {
					try {
						Set<Step> set = getSteps(monitor);
						steps.set(set);
						for (IStepListener listener : stepDefinitionsListeners) {
							if (listener == ExtensionRegistryStepProvider.this) {
								continue;
							}
						}
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, "cucumber.eclipse.editor", "reloading step definitions failed ("+e+")", e);
					}
					
				} catch(OperationCanceledException oce) {
					return Status.CANCEL_STATUS;
				}
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
	     };
	     if(jobChangeListener != null) {
	    	 job.addJobChangeListener(jobChangeListener);
	     }
	     job.setUser(true);
	     job.schedule();
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
			 System.out.println("ExtensionRegistryStepProvider scans step definitions for " + file.getName() + " in " + (end - start) + " ms. " + stringBuffer.toString());
		}
		return newSteps;
	}

	public void removeStepListener(IStepListener listener) {
		stepDefinitionsListeners.remove(listener);
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.removeStepListener(listener);
		}
	}

}
