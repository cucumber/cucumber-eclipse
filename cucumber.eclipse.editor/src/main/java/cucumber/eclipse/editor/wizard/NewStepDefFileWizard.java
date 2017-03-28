package cucumber.eclipse.editor.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import cucumber.eclipse.editor.util.Utils;


/**
 * @author girija.panda@nokia.com
 * 
 * Purpose: For creating Step definition(java) file
 * User can create a new  Step definition(java) file from Eclipse Menu : 
 * File > New > Other > Cucumber > Step-Definition class > Next >
 * Specify: Source Folder:<browse project dir>
 * 			Package Name:<browse package name>
 * 			Class Name:<input file name>
 * Select :	Cucumber Annotations : Given/When/Then/And/But
 * Result : Sample Step definition(java) file is created for selected Annotations
 * 
 */
public class NewStepDefFileWizard extends Wizard implements INewWizard {

	private NewStepDefFileWizardPage c_stepDefPage;
	
	private static final String CUCUMBER_PACKAGE = "import cucumber.api.java.en.";
	
	private static final String ANNOTATION_START = "  @" ;
	private static final String ANNOTATION_CONTENT_START = "(\"^you are in ";
	private static final String ANNOTATION_CONTENT_END = " annotation$\")";
	
	private static final String METHOD_START = "  public void ";
	private static final String METHOD_SIGNATURE = "() ";
	private static final String METHOD_EXCEPTION = "throws Throwable";
	private static final String METHOD_DEFINITION=" {\n" + "  }\n\n";
	
	private static final String PACKAGE_KEYWORD = "package ";
	
	private static final String CLASS_DECLARATION = "public class ";
	private static final String CLASS_START = " {\n";
	private static final String CLASS_END = "}\n";
	

	//Initialize
	public NewStepDefFileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		//System.out.println("NewStepDefFileWizard:addPages() Inside....");
		//super.addPages(); // new added
		this.c_stepDefPage = new NewStepDefFileWizardPage();
		addPage(this.c_stepDefPage);
		//System.out.println("NewStepDefFileWizard:addPages() END....");
	}

	@Override
	public boolean performFinish() {

		//System.out.println("NewStepDefFileWizard:performFinish() Inside....");
		String containerName = this.c_stepDefPage.getSourceFolder();
		String className = this.c_stepDefPage.getClassName();
		String packageName = this.c_stepDefPage.getPackageName();
		try 
		{		
			return doFinish(containerName, packageName, className, null, null, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		//System.out.println("NewStepDefFileWizard:performFinish() END....");
		return true;
	}

	private boolean doFinish(String containerName, String packageName,
			String className, String xmlPath, List<IMethod> methods,
			IProgressMonitor monitor) throws CoreException 
	{
		//System.out.println("NewFeatureFileWizard:doFinish() Inside....");
		boolean result = true;
		
		//validate file name
		if(className.contains(".java")){			
			//className = className.substring(0, className.indexOf("."));			
			className = className.replaceAll(".java", "");
		}		
		
		if (result) 
		{
			IFile file = createFile(containerName, packageName, className+ ".java", createJavaContentStream(className, methods), monitor);
			if (file != null) {
				Utils.openFile(getShell(), file, monitor);
			} else {
				result = false;
			}
		}
		//System.out.println("NewFeatureFileWizard:doFinish() END....");
		return result;
	}

	// Create a New Step Definition Java File by new file wizard
	private IFile createFile(String containerName, String packageName,
			String fileName, InputStream contentStream, IProgressMonitor monitor)
			throws CoreException 
	{
		//System.out.println("NewFeatureFileWizard:createFile() Inside....");	
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String fullPath = fileName;
		
		if ((packageName != null) && (!"".equals(packageName))) {
			fullPath = packageName.replace(".", File.separator) + File.separatorChar + fileName;
		}
		
		Path absolutePath = new Path(containerName + File.separatorChar + fullPath);
		IFile result = root.getFile(absolutePath);
		Utils.createFileWithDialog(getShell(), result, contentStream);
		//System.out.println("NewFeatureFileWizard:createFile() END....");
		return result;
	}

	//Creating Step-Definition file content
	private InputStream createJavaContentStream(String className, List<IMethod> testMethods) 
	{
		
		StringBuilder imports = new StringBuilder();
		StringBuilder methods = new StringBuilder();
		if(className.contains(".java")){		
			//className = className.substring(0, className.indexOf("."));			
			className = className.replaceAll(".java", "");
		}
		
		//Create Methods for selected Cucumber Annotations
		for (String a : NewStepDefFileWizardPage.ANNOTATIONS) 
		{
			if ((!"".equals(a)) && (this.c_stepDefPage.containsAnnotation(a))) 
			{
				imports.append(CUCUMBER_PACKAGE + a + ";\n");	
				methods.append(ANNOTATION_START + a + ANNOTATION_CONTENT_START + a + ANNOTATION_CONTENT_END +"\n" 
							  + METHOD_START + toMethod(a) + METHOD_SIGNATURE + METHOD_EXCEPTION 
							  	+ METHOD_DEFINITION);
			}
		}
		
		//package <name>+import <package>+class <class-name> {+ methods()+}
		String contents =  PACKAGE_KEYWORD + this.c_stepDefPage.getPackage() + ";\n\n"
						 + imports + "\n" + CLASS_DECLARATION 
						 + className + CLASS_START
						 + methods 
						 + CLASS_END;
		
		return new ByteArrayInputStream(contents.getBytes());
	}

	private String toMethod(String a) {
		return Character.toLowerCase(a.charAt(0)) + a.substring(1);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {}

}
