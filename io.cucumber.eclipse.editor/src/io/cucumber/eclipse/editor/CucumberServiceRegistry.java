package io.cucumber.eclipse.editor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.cucumber.eclipse.editor.hyperlinks.IStepDefinitionOpener;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;

/**
 * The {@link CucumberServiceRegistry} gives access to the extensions provided
 * by language providers
 * 
 * @author christoph
 *
 */
@Component(service = {})
public class CucumberServiceRegistry {

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private final List<IStepDefinitionsProvider> stepDefinitionsProvider = new CopyOnWriteArrayList<>();
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private final List<IStepDefinitionOpener> stepDefinitionOpener = new CopyOnWriteArrayList<>();

	private static final AtomicReference<CucumberServiceRegistry> REGISTRY = new AtomicReference<>();

	@Activate
	void start() {
		REGISTRY.set(this);
	}

	@Deactivate
	void stop() {
		REGISTRY.compareAndSet(this, null);
	}

	private static CucumberServiceRegistry get() {
		return Objects.requireNonNullElseGet(REGISTRY.get(), CucumberServiceRegistry::new);
	}

	public static List<IStepDefinitionOpener> getStepDefinitionOpener() {
		return Collections.unmodifiableList(get().stepDefinitionOpener);
	}

	public static List<IStepDefinitionsProvider> getStepDefinitionsProvider(IResource resource) {
		// TODO better pass the document?!
		return get().stepDefinitionsProvider.stream().filter(p -> {
			try {
				return p.support(resource);
			} catch (CoreException e) {
				return false;
			}
		}).collect(Collectors.toUnmodifiableList());
	}
}
