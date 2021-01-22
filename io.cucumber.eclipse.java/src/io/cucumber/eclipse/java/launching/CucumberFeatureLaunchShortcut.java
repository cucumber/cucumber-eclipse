package io.cucumber.eclipse.java.launching;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.resource.Resource;
import io.cucumber.core.runtime.FeatureSupplier;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberStepParserPlugin;
import mnita.ansiconsole.preferences.AnsiConsolePreferenceUtils;

public class CucumberFeatureLaunchShortcut implements ILaunchShortcut2 {

	private String newLaunchConfigurationName;

	@Override
	public void launch(IEditorPart editor, String mode) {
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			IEditorInput editorInput = textEditor.getEditorInput();
			IDocument document = textEditor.getDocumentProvider().getDocument(editorInput);
			System.out.println(editorInput);
//			ITextViewer textViewer = (ITextViewer) editor;
//			IDocument document = textViewer.getDocument();
			if (document != null) {
				GherkinEditorDocument gherkinEditorDocument = GherkinEditorDocument.get(document);
				System.out.println(gherkinEditorDocument);
			}
		}
		// TODO Auto-generated method stub
		System.out.println("CucumberFeatureLaunchShortcut.launch() editor=" + editor + ", mode = " + mode);

	}

	@Override
	public void launch(ISelection selection, String mode) {

		System.out.println("CucumberFeatureLaunchShortcut.launch() selection=" + selection + ", mode = " + mode);
		if (selection instanceof StructuredSelection) {
			List<IFile> resources = Arrays.stream(((StructuredSelection) selection).toArray())
					.map(o -> Adapters.adapt(o, IFile.class)).collect(Collectors.toList());
			Job job = Job.create("Executing " + resources.size() + " Feature(s)", new ICoreRunnable() {

				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					SubMonitor subMonitor = SubMonitor.convert(monitor, resources.size() * 100);
					for (IFile resource : resources) {
						runFeature(resource, subMonitor.split(100, SubMonitor.SUPPRESS_NONE));
					}

				}
			});
			job.schedule();
		}
	}

	protected void runFeature(IFile resource, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Running " + resource.getName(), 100);
		IJavaProject javaProject = JDTUtil.getJavaProject(resource);
		if (javaProject == null) {
			return;
		}
		// FIXME create a CucumberRuntimeJob
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		URLClassLoader classloader = JDTUtil.createClassloader(javaProject);
		try {
			Thread.currentThread().setContextClassLoader(classloader);
			// TODO configure add plugins for output progres......
			RuntimeOptionsBuilder runtimeOptions = new RuntimeOptionsBuilder()//
					.addDefaultGlueIfAbsent()//
					.setThreads(java.lang.Runtime.getRuntime().availableProcessors())//
					.setMonochrome(!AnsiConsolePreferenceUtils.isAnsiConsoleEnabled())
					.addDefaultSummaryPrinterIfAbsent()//
			// .setDryRun();
			;

			CucumberStepParserPlugin stepParserPlugin = new CucumberStepParserPlugin();
			final Runtime runtime = Runtime.builder()//
					.withRuntimeOptions(runtimeOptions.build())//
					.withClassLoader(() -> classloader)//
					.withFeatureSupplier(new FeatureSupplier() {

						@Override
						public List<Feature> get() {
							FeatureParser parser = new FeatureParser(UUID::randomUUID);
							Optional<Feature> optional = parser.parseResource(new Resource() {
								
								@Override
								public URI getUri() {
									return resource.getLocationURI();
								}
								
								@Override
								public InputStream getInputStream() throws IOException {
									try {
										return resource.getContents();
									} catch (CoreException e) {
										throw new IOException(e);
									}
								}
							});
							return Collections.singletonList(optional.get());
						}
					})//
					.withAdditionalPlugins(stepParserPlugin)//
					.build();
			runtime.run();
		} finally {
			Thread.currentThread().setContextClassLoader(ccl);
			try {
				classloader.close();
			} catch (IOException e) {
				Activator.getDefault().getLog().warn("Closing classloader failed", e);
			}
		}

	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		// TODO Auto-generated method stub
		System.out.println("CucumberFeatureLaunchShortcut.getLaunchConfigurations() selection=" + selection);
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		// TODO Auto-generated method stub
		System.out.println("CucumberFeatureLaunchShortcut.getLaunchConfigurations() editorpart=" + editorpart);
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		// TODO Auto-generated method stub
		System.out.println("CucumberFeatureLaunchShortcut.getLaunchableResource() selection=" + selection);
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		// TODO Auto-generated method stub
		System.out.println("CucumberFeatureLaunchShortcut.getLaunchableResource() editorpart=" + editorpart);
		return null;
	}

//	@Override
//	public void launch(ISelection selection, String arg1) {
//		// TODO Auto-generated method stub
//		
//	}
//
//  	@Override
//	public void launch(IEditorPart part, String mode) {
//  		newLaunchConfigurationName = part.getTitle();
////		launch(mode);
//	}

//	@Override
//	protected String getLaunchConfigurationTypeName() {
//		return CucumberFeatureLaunchConstants.CUCUMBER_FEATURE_LAUNCH_CONFIG_TYPE;
//	}
//
//	@Override
//	protected void initializeConfiguration(ILaunchConfigurationWorkingCopy config) {
//		IProject project = CucumberFeatureLaunchUtils.getProject();
//		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, CucumberFeatureLaunchUtils.getFeaturePath());
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, CucumberFeatureLaunchConstants.DEFAULT_CLASSPATH);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, true);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY, true);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, false);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, false);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, false);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, false);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, false);
//		config.setAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, false);
//		
//	}
//	
//	@Override
//	protected String getName(ILaunchConfigurationType type) {
//		if(newLaunchConfigurationName != null) {
//			return newLaunchConfigurationName;
//		}
//		return super.getName(type);
//	}
//
//	
//	@Override
//	protected boolean isGoodMatch(ILaunchConfiguration configuration) {
//		boolean goodType = isGoodType(configuration);
//		boolean goodName = isGoodName(configuration);
//		return goodType && goodName;
//	}

//	private boolean isGoodName(ILaunchConfiguration configuration) {
//		return configuration.getName().equals(newLaunchConfigurationName);
//	}
//
//	private boolean isGoodType(ILaunchConfiguration configuration) {
//		try {
//			String identifier = configuration.getType().getIdentifier();
//			return CucumberFeatureLaunchConstants.CUCUMBER_FEATURE_LAUNCH_CONFIG_TYPE.equals(identifier);
//		} catch (CoreException e) {
//			return false;
//		}
//	}

}
