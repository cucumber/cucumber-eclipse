package cucumber.eclipse.editor.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

public class ProjectGlueCodeOptions extends PropertyPage implements IWorkbenchPropertyPage {

	private Button matchAllParameterButton;

	private static final String NAMESPACE = "cucumber.steps.glue";
	private static final String KEY_MATCH_ALL_PARAMETER = "matchAllParameter";

	public ProjectGlueCodeOptions() {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		matchAllParameterButton = createEnableOption(composite);
		return composite;
	}

	private Button createEnableOption(Composite composite) {
		Button button = new Button(composite, SWT.CHECK);
		GridData gd = new GridData();
		button.setLayoutData(gd);
		gd.horizontalSpan = 2;
		button.setText("Match regardless of parametertype");
		button.setSelection(isMatchAllParameter(getResource()));
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		return button;
	}

	private IResource getResource() {
		return getElement().getAdapter(IResource.class);
	}

	private static IEclipsePreferences getNode(IResource resource) {
		ProjectScope scope = new ProjectScope(resource.getProject());
		IEclipsePreferences node = scope.getNode(NAMESPACE);
		return node;
	}

	protected void performDefaults() {
		super.performDefaults();
		matchAllParameterButton.setSelection(false);
	}

	public boolean performOk() {
		IResource resource = getResource();
		IEclipsePreferences node = getNode(resource);
		boolean oldvalue = isMatchAllParameter(resource);
		boolean newValue = matchAllParameterButton.getSelection();
		if (newValue != oldvalue) {
			node.putBoolean(KEY_MATCH_ALL_PARAMETER, newValue);
			try {
				node.flush();
			} catch (BackingStoreException e) {
			}
			IProject project = resource.getProject();
			Job.create("Rebuild of " + project.getName(), new ICoreRunnable() {

				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				}
			}).schedule();
		}
		return true;
	}

	public static boolean isMatchAllParameter(IResource resource) {
		IEclipsePreferences node = getNode(resource);
		return node.getBoolean(KEY_MATCH_ALL_PARAMETER, false);
	}

}
