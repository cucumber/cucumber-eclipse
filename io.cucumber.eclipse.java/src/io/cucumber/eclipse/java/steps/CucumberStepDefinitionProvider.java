package io.cucumber.eclipse.java.steps;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextViewer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;
import io.cucumber.eclipse.editor.steps.ExpressionDefinition;
import io.cucumber.eclipse.editor.steps.IStepDefinitionsProvider;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;
import io.cucumber.eclipse.java.plugins.CucumberStepDefinition;
import io.cucumber.eclipse.java.runtime.CucumberRuntime;
import io.cucumber.eclipse.java.validation.JavaGlueValidator;

/**
 * Step definition provider that calls cucumber to find steps for the project
 * 
 * @author christoph
 *
 */
@Component(service = IStepDefinitionsProvider.class, property = {
		IStepDefinitionsProvider.PROVIDER_NAME + "=Cucumber JVM Runtime", Constants.SERVICE_RANKING + ":Integer=100" })
public class CucumberStepDefinitionProvider extends JavaStepDefinitionsProvider {

	private Feature dummyFeature;

	public CucumberStepDefinitionProvider() throws URISyntaxException {
		URI uri = new URI("dummy:uri");
		dummyFeature = CucumberRuntime.loadFeature(new Resource() {

			@Override
			public URI getUri() {
				return uri;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream("Feature: Dummy\r\nScenario: Dummy\r\nGiven a dummy".getBytes());
			}
		}).get();
	}

	@Override
	public Collection<StepDefinition> findStepDefinitions(ITextViewer viewer, int offset, IResource resource,
			IProgressMonitor monitor) throws CoreException {
		try {
			IJavaProject javaProject = JDTUtil.getJavaProject(resource);
			SubMonitor subMonitor = SubMonitor.convert(monitor, "Searching Java Glue Code steps", 200);
			Collection<CucumberStepDefinition> steps = JavaGlueValidator.getAvailableSteps(viewer.getDocument(),
					subMonitor.split(100));
			SubMonitor remaining = subMonitor.setWorkRemaining(steps.size());
			Map<String, IType> typeBuffer = new ConcurrentHashMap<>();
			return steps.parallelStream()
					.map(cucumberStep -> parseStepDefintion(cucumberStep, javaProject, typeBuffer, remaining.split(1)))
					.filter(Objects::nonNull).collect(Collectors.toList());
		} catch (OperationCanceledException e) {
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return Collections.emptyList();
	}

	private StepDefinition parseStepDefintion(CucumberStepDefinition cucumberStep, IJavaProject project,
			Map<String, IType> typeBuffer, IProgressMonitor monitor) {
		CucumberCodeLocation codeLocation = cucumberStep.getCodeLocation();
		io.cucumber.plugin.event.StepDefinition cucumberStepDefinition = cucumberStep.getStepDefinition();
		IType type = typeBuffer.computeIfAbsent(codeLocation.getTypeName(), typeName -> {
			try {
				return project.findType(typeName, monitor);
			} catch (JavaModelException e) {
				return null;
			}
		});
		if (type != null) {
			try {
				IMethod[] methods = JDTUtil.resolveMethod(project, codeLocation, monitor);
				if (methods.length == 1) {
					
					
					// perfect match
					IMethod method = methods[0];
					int lineNumber = getLineNumber(method.getCompilationUnit(), method);
					ExpressionDefinition expression = new ExpressionDefinition(cucumberStepDefinition.getPattern());
					String id = method.getHandleIdentifier();
					return new StepDefinition(id, JDTUtil.getMethodName(method), expression,
							type.getResource(), lineNumber, method.getElementName(),
							type.getPackageFragment().getElementName(), getParameter(method),
							JDTUtil.getJavadoc(method));
				}
			} catch (JavaModelException e) {
			}
		}
		return new StepDefinition(cucumberStepDefinition.getLocation(), StepDefinition.NO_LABEL,
				new ExpressionDefinition(cucumberStepDefinition.getPattern()), null, -1,
				cucumberStepDefinition.getLocation(), "", null, null);
	}

}
