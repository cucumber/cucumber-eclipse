package cucumber.eclipse.launching;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class CucumberMainTab  
extends SharedJavaMainTab
implements ILaunchConfigurationTab {

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		createProjectEditor(comp);
		setControl(comp);
		IProject project = getProject();
		if (project!=null) {
			fProjText.setText(getProject().getName());
		}
	}

	@Override
	public String getName() {
		return "Cucumber Feature Runner";
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
        mapResources(config);
		
	}

	
	protected IProject getProject() {
		
        IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
        if (page != null) {
          IEditorPart part = page.getActiveEditor();
        			if(part  != null) {
        			    IFileEditorInput input = (IFileEditorInput)part.getEditorInput() ;
        			    IFile file = input.getFile();
        			    IProject activeProject = file.getProject();
        			    return activeProject;
        			}
        }

       return null;
    }
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
	
	     IProject javaProject = getProject();
	        if (javaProject!= null) {
	       	 initializeJavaProject(javaProject, config);
	        }
	        else {
	         config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
	        }
	        
	}

	@Override
	protected void handleSearchButtonSelected() {
		
	}
	
	@Override
	public void initializeFrom(ILaunchConfiguration config) {		

	}
	
	   protected void initializeJavaProject(IProject javaProject, ILaunchConfigurationWorkingCopy config) {
	        
	        String name = null;
	        if (javaProject != null && javaProject.exists()) {
	            name = javaProject.getName();
	        }
	        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	        
	    } 





}


