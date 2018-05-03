package cucumber.eclipse.editor.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
//import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class Utils {

	private static List<JavaElement> result = null;
	private static ISelection selection = null;
	private static TreeSelection treeselection = null;
	private static IEditorReference[] editors = null;
	private static IEditorPart editor = null;
	private static ITypeRoot root = null;

	
	public Utils() {
		// TODO Auto-generated constructor stub
	}

	
	
	 public static List<JavaElement> getSelectedJavaElements()
	  {
		 //System.out.println("Utils:getSelectedJavaElements-1 Inside....");
	    return getSelectedJavaElements(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
	  }
	  
	  public static List<JavaElement> getSelectedJavaElements(IWorkbenchPage page)
	  {
	   
		//System.out.println("Utils:getSelectedJavaElements-2 Inside....");
		result = new ArrayList<JavaElement>();
	    selection = page.getSelection();
	    if ((selection instanceof TreeSelection))
	    {
	    	treeselection = (TreeSelection)selection;
	      for (Iterator it = treeselection.iterator(); it.hasNext();) {
	        result.add(convertToJavaElement(it.next()));
	      }
	    }
	    else
	    {
	      editors = page.getEditorReferences();
	      for (IEditorReference ref : editors)
	      {
	        editor = ref.getEditor(false);
	        if (editor != null)
	        {
	          root = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
	          if ((root != null) && (root.getElementType() == 5)) {
	            result.add(convertToJavaElement(root));
	          }
	        }
	      }
	    }
	    return result;
	  }
	  
	  
	  private static JavaElement convertToJavaElement(Object element)
	  {
		  //System.out.println("Utils.convertToJavaElement()");
	    JavaElement result = new JavaElement();
	    if ((element instanceof IFile))
	    {
	      IJavaElement je = JavaCore.create((IFile)element);
	      if ((je instanceof ICompilationUnit)) {
	        result.compilationUnit = ((ICompilationUnit)je);
	      }
	    }
	    else if ((element instanceof ICompilationUnit))
	    {
	      result.compilationUnit = ((ICompilationUnit)element);
	    }
	    else if ((element instanceof IPackageFragment))
	    {
	      result.packageFragment = ((IPackageFragment)element);
	    }
	    else if ((element instanceof IPackageFragmentRoot))
	    {
	      result.packageFragmentRoot = ((IPackageFragmentRoot)element);
	    }
	    else if ((element instanceof IJavaProject))
	    {
	      result.m_project = ((IJavaProject)element);
	    }
	    else if ((element instanceof IProject))
	    {
	      result.m_project = JavaCore.create((IProject)element);
	    }
	    IResource resource = result.getResource();
	    if (resource != null)
	    {
	      result.sourceFolder = resource.getFullPath().removeLastSegments(1).toString();
	     // System.out.println("result.sourceFolder-1=" +result.sourceFolder);
	     
	      for (IClasspathEntry entry : getSourceFolders(result.getProject()))
	      {
	        String source = entry.getPath().toString();
	        
	        System.out.println("Source =" +source);
	        
	        if (source.endsWith("src/test/java"))
	        {
	          result.sourceFolder = source;
	          break;
	        }
	        if (source.contains("test"))
	        {
	          result.sourceFolder = source;
	          break;
	        }
	        
	        //System.out.println("ResourceFullpath =" +resource.getFullPath().toString());
	        
	        if (resource.getFullPath().toString().startsWith(source))
	        {
	          result.sourceFolder = source;
	          break;
	        }
	      }
	      
	     // System.out.println("result.sourceFolder-2 =" +result.sourceFolder);
	      if (result.sourceFolder.endsWith("src/main/java")) 
	      {
	    	 
	        result.sourceFolder = result.sourceFolder.replace("main", "test");
	      }
	    }
	    return result;
	  }
	  
	  public static List<IClasspathEntry> getSourceFolders(IJavaProject jp)
	  {
	    //List<IClasspathEntry> result = Lists.newArrayList();
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
	    try
	    {
	      for (IClasspathEntry entry : jp.getRawClasspath()) {
	        if (entry.getEntryKind() == 3) {
	          result.add(entry);
	        }
	      }
	    }
	    catch (JavaModelException e)
	    {
	      e.printStackTrace();
	    }
	    return result;
	  }
	  
	  public static void openFile(Shell shell, final IFile javaFile, IProgressMonitor monitor)
	  {
	    monitor.setTaskName("Opening file for editing...");
	    shell.getDisplay().asyncExec(new Runnable()
	    {
	      public void run()
	      {
	        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        try
	        {
	          //IDE.openEditor(page, Utils.this, true);
	          IDE.openEditor(page, javaFile, true);
	        }
	        catch (PartInitException localPartInitException) {}
	      }
	    });
	    monitor.worked(1);
	  }
	
	  public static boolean createFileWithDialog(Shell shell, IFile file, InputStream stream)
			    throws CoreException
			  {
			    boolean success = false;
			    NullProgressMonitor monitor = new NullProgressMonitor();
			    try
			    {
			      if (file.exists())
			      {
			        boolean overwrite = MessageDialog.openConfirm(shell, 
			        		"Step-Definition File alreadyExists ",
			        		"Step-Definition File {0} already exists. Overwrite it?");
			        if (overwrite)
			        {
			          file.setContents(stream, true, true, monitor);
			          success = true;
			        }
			      }
			      else
			      {
			        createResourceRecursively(file, monitor);
			        file.setContents(stream, 3, monitor);
			        success = true;
			      }
			      stream.close();
			    }
			    catch (IOException localIOException) {}
			    return success;
			  
			 }
	  
	  protected static void createResourceRecursively(IResource resource, IProgressMonitor monitor)
			    throws CoreException
			  {
			    if ((resource == null) || (resource.exists())) {
			      return;
			    }
			    if (!resource.getParent().exists()) {
			      createResourceRecursively(resource.getParent(), monitor);
			    }
			    switch (resource.getType())
			    {
			    case 1: 
			      ((IFile)resource).create(new ByteArrayInputStream(new byte[0]), true, monitor);
			      break;
			    case 2: 
			      ((IFolder)resource).create(0, true, monitor);
			      break;
			    case 4: 
			      ((IProject)resource).create(monitor);
			      ((IProject)resource).open(monitor);
			    }
			  }
	  
	  
	  
	  
	  
	  
	  
	 public static class JavaElement
	  {
	    public IJavaProject m_project;
	    public IPackageFragmentRoot packageFragmentRoot;
	    public IPackageFragment packageFragment;
	    public ICompilationUnit compilationUnit;
	    public String sourceFolder;
	    
	    public String getPath()
	    {
	      String result = null;
	      if (this.compilationUnit != null) {
	        result = resourceToPath(this.compilationUnit);
	      } else if (this.packageFragmentRoot != null) {
	        result = resourceToPath(this.packageFragmentRoot);
	      } else if (this.packageFragment != null) {
	        result = resourceToPath(this.packageFragment);
	      } else {
	        result = resourceToPath(getProject());
	      }
	      return result;
	    }
	    
	    public IJavaProject getProject()
	    {
	      if (this.m_project != null) {
	        return this.m_project;
	      }
	      if (this.packageFragmentRoot != null) {
	        return this.packageFragmentRoot.getJavaProject();
	      }
	      if (this.packageFragment != null) {
	        return this.packageFragment.getJavaProject();
	      }
	      if (this.compilationUnit != null) {
	        return this.compilationUnit.getJavaProject();
	      }
	      throw new AssertionError("Couldn't find a project");
	    }
	    
	    private String resourceToPath(IJavaElement element)
	    {
	      return ((IResource)element.getAdapter(IResource.class)).getFullPath().toOSString();
	    }
	    
	    public String getPackageName()
	    {
	      String result = null;
	      if (this.packageFragment != null) {
	        result = this.packageFragment.getElementName();
	      } else if (this.compilationUnit != null) {
	        try
	        {
	          IPackageDeclaration[] pkg = this.compilationUnit.getPackageDeclarations();
	          result = pkg.length > 0 ? pkg[0].getElementName() : null;
	        }
	        catch (JavaModelException localJavaModelException) {}
	      }
	      return result;
	    }
	    
	    public String getClassName()
	    {
	      String result = null;
	      if (this.compilationUnit != null)
	      {
	        result = this.compilationUnit.getElementName();
	        if (result.endsWith(".java")) {
	          result = result.substring(0, result.length() - ".java".length());
	        }
	      }
	      return result;
	    }
	    
	    public IResource getResource()
	    {
	      if (this.compilationUnit != null) {
	        return (IResource)this.compilationUnit.getAdapter(IResource.class);
	      }
	      if (this.packageFragment != null) {
	        return (IResource)this.packageFragment.getAdapter(IResource.class);
	      }
	      if (this.m_project != null) {
	        return (IResource)this.m_project.getAdapter(IResource.class);
	      }
	      return null;
	    }
	  }
}
