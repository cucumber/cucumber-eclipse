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
import org.eclipse.core.runtime.SubMonitor;

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
		//TODO can we obtain a progressmonitor somewhere?
		Set<Step> set = getSteps(null);
		steps.set(set);
		for (IStepListener listener : listeners) {
			if (listener == this) {
				continue;
			}
			listener.onStepsChanged(new StepsChangedEvent());
		}
	}

	public Set<Step> getSteps(IProgressMonitor progressMonitor) {
		Set<Step> newSteps = new LinkedHashSet<Step>();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, "Reloading steps", stepDefinitions.size());
		try {
			for (IStepDefinitions stepDef : stepDefinitions) {
				try {
					newSteps.addAll(stepDef.getSteps(file, subMonitor.split(1)));
				} catch (CoreException e) {
					e.printStackTrace();
				}
				if (subMonitor.isCanceled()) {
					return Collections.emptySet();
				}
			}
		} finally {
			SubMonitor.done(progressMonitor);
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
