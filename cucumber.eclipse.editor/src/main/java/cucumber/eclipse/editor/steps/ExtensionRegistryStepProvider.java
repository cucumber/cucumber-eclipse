package cucumber.eclipse.editor.steps;

import static cucumber.eclipse.editor.util.ExtensionRegistryUtil.getIntegrationExtensionsOfType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.eclipse.core.runtime.jobs.Job;
import cucumber.eclipse.steps.integration.IStepDefinitions;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import cucumber.eclipse.steps.integration.StepsChangedEvent;

public class ExtensionRegistryStepProvider implements IStepProvider, IStepListener {

	private AtomicReference<Set<Step>> steps = new AtomicReference<Set<Step>>(Collections.<Step>emptySet());

	private List<IStepDefinitions> stepDefinitions = getIntegrationExtensionsOfType(IStepDefinitions.class);
	
	private List<IStepListener> listeners = new CopyOnWriteArrayList<IStepListener>();

	private IFile file;
	
	public ExtensionRegistryStepProvider(IFile file) {
		this.file = file;
		addStepListener(this);
	}

	public void addStepListener(IStepListener listener) {
		listeners.add(listener);
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.addStepListener(listener);
		}
	}

	public Set<Step> getStepsInEncompassingProject() {
		return steps.get();
	}
	
	/**
	 * Asyncrounous load steps and notify listeners
	 */
	public void reload() {
		final Job job = new Job("Scanning for step definitions") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					try {
						Set<Step> set = getSteps(monitor);
						steps.set(set);
						for (IStepListener listener : listeners) {
							if (listener == ExtensionRegistryStepProvider.this) {
								continue;
							}
							listener.onStepsChanged(new StepsChangedEvent());
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
	     job.setUser(true);
	     job.schedule();
	}

	public Set<Step> getSteps(IProgressMonitor progressMonitor) throws CoreException {
		Set<Step> newSteps = new LinkedHashSet<Step>();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, "Reloading steps", stepDefinitions.size());
		try {
			for (IStepDefinitions stepDef : stepDefinitions) {
				newSteps.addAll(stepDef.getSteps(file, subMonitor.newChild(1)));
				if (subMonitor.isCanceled()) {
					return Collections.emptySet();
				}
			}
		} finally {
			 if (progressMonitor != null) {
				 progressMonitor.done();
	         }
		}
		return newSteps;
	}

	public void removeStepListener(IStepListener listener) {
		listeners.remove(listener);
		for (IStepDefinitions stepDef : stepDefinitions) {
			stepDef.removeStepListener(listener);
		}
	}

	@Override
	public void onStepsChanged(StepsChangedEvent event) {
		reload();
	}
}
