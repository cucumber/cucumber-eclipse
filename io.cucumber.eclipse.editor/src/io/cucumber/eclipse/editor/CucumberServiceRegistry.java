package io.cucumber.eclipse.editor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;

import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;
import io.cucumber.eclipse.editor.launching.ILauncher;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.validation.IGlueValidator;

/**
 * The {@link CucumberServiceRegistry} gives access to the extensions provided
 * by language providers
 * 
 * @author christoph
 *
 */
@Component(service = { CucumberServiceRegistry.class })
public class CucumberServiceRegistry {

	private static ServiceTracker<CucumberServiceRegistry, CucumberServiceRegistry> tracker;
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private final List<IStepDefinitionsProvider> stepDefinitionsProvider = new CopyOnWriteArrayList<>();
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private final List<IStepDefinitionOpener> stepDefinitionOpener = new CopyOnWriteArrayList<>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private final List<ILauncher> cucumberLauncher = new CopyOnWriteArrayList<>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private final List<IGlueValidator> glueValidators = new CopyOnWriteArrayList<>();

	public static List<IStepDefinitionOpener> getStepDefinitionOpener() {
		CucumberServiceRegistry service = Activator.getService();
		if (service == null) {
			return List.of();
		}
		return Collections.unmodifiableList(service.stepDefinitionOpener);
	}

	public static List<ILauncher> getLauncher() {
		CucumberServiceRegistry service = Activator.getService();
		if (service == null) {
			return List.of();
		}
		return Collections.unmodifiableList(service.cucumberLauncher);
	}

	public static List<IStepDefinitionsProvider> getStepDefinitionsProvider(IResource resource) {
		CucumberServiceRegistry service = Activator.getService();
		if (service == null) {
			return List.of();
		}
		return service.stepDefinitionsProvider.stream().filter(p -> {
			try {
				return p.support(resource);
			} catch (CoreException e) {
				return false;
			}
		}).collect(Collectors.toUnmodifiableList());
	}

	public static List<IGlueValidator> getGlueValidators(IResource resource) {
		CucumberServiceRegistry service = Activator.getService();
		if (service == null) {
			return List.of();
		}
		return service.glueValidators.stream().filter(v -> {
			try {
				return v.canValidate(resource);
			} catch (CoreException e) {
				return false;
			}
		}).collect(Collectors.toUnmodifiableList());
	}
}
