package io.cucumber.eclipse.editor.steps;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import io.cucumber.eclipse.editor.ExtensionRegistryUtil;
import io.cucumber.eclipse.editor.ResourceHelper;
import io.cucumber.eclipse.editor.StorageHelper;

/**
 * Storage class for step definitions.
 * 
 * Step definitions are store by resource name in order to be able to update
 * them partially.
 * 
 * Thus, when we computes step definitions from only one resource, the previous
 * computed step definitions for this resource are overwritten. And we don't
 * have to compute step definitions for all projects to have a real view of step
 * definitions.
 * 
 * 
 * @author qvdk
 *
 */
public class StepDefinitionsRepository implements Externalizable {

	private static final long serialVersionUID = -5426643465030832989L;
	private final Map<IResource, Set<StepDefinition>> stepDefinitionsByResourceName = new HashMap<IResource, Set<StepDefinition>>();
	public synchronized void add(IResource stepDefinitionsFile, Set<StepDefinition> steps) {
		if (steps.isEmpty()) {
			this.stepDefinitionsByResourceName.remove(stepDefinitionsFile);
		} else {
			this.stepDefinitionsByResourceName.put(stepDefinitionsFile, steps);
		}
	}

	public Set<IFile> getAllStepDefinitionsFiles() {
		Set<IFile> fromFilesOnly = new HashSet<IFile>();
		for (IResource resource : this.stepDefinitionsByResourceName.keySet()) {
			if (resource instanceof IFile) {
				fromFilesOnly.add((IFile) resource);
			}
		}
		return fromFilesOnly;
	}

	public Set<StepDefinition> getAllStepDefinitions() {
		Set<StepDefinition> allSteps = new HashSet<StepDefinition>();
		for (Set<StepDefinition> steps : stepDefinitionsByResourceName.values()) {
			allSteps.addAll(steps);
		}
		return allSteps;
	}

	public boolean isStepDefinitionsResource(IResource resource) {
		return this.stepDefinitionsByResourceName.containsKey(resource);
	}

	public void reset() {
		stepDefinitionsByResourceName.clear();
	}


	@Override
	public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		stepDefinitionsByResourceName.clear();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			String key = (String) in.readObject();
			IResource resource = ResourceHelper.find(key);
			int childSize = in.readInt();
			Set<StepDefinition> steps = new HashSet<>();
			for (int j = 0; j < childSize; j++) {
				StepDefinition step = StorageHelper.readStepDefinition(in);
				if (step != null) {
					steps.add(step);
				}
			}
			// only add data for existing resources...
			if (resource != null && !steps.isEmpty()) {
				stepDefinitionsByResourceName.put(resource, steps);
			}
		}
	}

	@Override
	public synchronized void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(stepDefinitionsByResourceName.size());
		for (Entry<IResource, Set<StepDefinition>> entry : stepDefinitionsByResourceName.entrySet()) {
			out.writeObject(entry.getKey().getFullPath().toString());
			Set<StepDefinition> value = entry.getValue();
			out.writeInt(value.size());
			for (StepDefinition stepDefinition : value) {
				StorageHelper.writeStepDefinition(stepDefinition, out);
			}
		}

	}

	public static Collection<StepDefinition> getStepDefinitions(IProject project, IProgressMonitor monitor)
			throws CoreException {
		return getStepDefinitions(project, new HashSet<>(), monitor);
	}

	private static Collection<StepDefinition> getStepDefinitions(IProject project, Set<String> analyzedProjects,
			IProgressMonitor monitor) throws CoreException {
		HashSet<StepDefinition> set = new HashSet<>();
		List<IStepDefinitionsProvider> provider = ExtensionRegistryUtil.getStepDefinitionsProvider();
		IProject[] referencedProjects = project.getReferencedProjects();
		SubMonitor subMonitor = SubMonitor.convert(monitor, (provider.size() + referencedProjects.length) * 100);
		for (IStepDefinitionsProvider definitionsProvider : provider) {
			set.addAll(definitionsProvider.findStepDefinitions(project, subMonitor.split(100)));
		}
		for (IProject referencedProject : referencedProjects) {
			if (!analyzedProjects.contains(referencedProject.getName())) {
				set.addAll(getStepDefinitions(project, analyzedProjects, subMonitor.split(100)));
			}
		}
		return set;
	}

}
