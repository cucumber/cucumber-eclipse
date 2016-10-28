package cucumber.eclipse.editor.util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class SWTUtil
{
  public static void setButtonGridData(Button button)
  {
    GridData gridData = new GridData();
    button.setLayoutData(gridData);
    setButtonDimensionHint(button);
  }
  
  public static int getButtonWidthHint(Button button)
  {
    button.setFont(JFaceResources.getDialogFont());
    PixelConverter converter = new PixelConverter(button);
    int widthHint = converter.convertHorizontalDLUsToPixels(61);
    return Math.max(widthHint, button.computeSize(-1, -1, true).x);
  }
  
  public static void setButtonDimensionHint(Button button)
  {
    //Assert.isNotNull(button);
    Object gd = button.getLayoutData();
    if ((gd instanceof GridData)) {
      ((GridData)gd).widthHint = getButtonWidthHint(button);
    }
  }
  
  public static Display getDisplay()
  {
    Display display = Display.getCurrent();
    if (display == null) {
      display = Display.getDefault();
    }
    return display;
  }
  
  public static IWorkbenchWindow getActiveWorkbenchWindow(IWorkbench workBench)
  {
    if (workBench == null) {
      return null;
    }
    return workBench.getActiveWorkbenchWindow();
  }
  
  public static IWorkbenchPage getActivePage(IWorkbench workBench)
  {
    IWorkbenchWindow activeWorkbenchWindow = getActiveWorkbenchWindow(workBench);
    if (activeWorkbenchWindow == null) {
      return null;
    }
    return activeWorkbenchWindow.getActivePage();
  }
  
  public static Composite createGridContainer(Composite parent, int columns)
  {
    Composite result = new Composite(parent, 0);
    createGridLayout(result, columns);
    return result;
  }
  
  public static void createGridLayout(Composite result, int columns)
  {
    GridLayout layout = new GridLayout();
    layout.numColumns = columns;
    result.setLayout(layout);
    
    GridData gd = new GridData(4, 4, true, true);
    result.setLayoutData(gd);
  }
  
  public static Text createPathBrowserText(final Composite container, String text, ModifyListener listener)
  {
    final Text result = createLabelText(container, text, listener);
    Button button = new Button(container, 8);
    button.setText("Browse...");
    button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(container.getShell(),
        		ResourcesPlugin.getWorkspace().getRoot(), false, "Select new file container");
        dialog.showClosedProjects(false);
        if (dialog.open() == 0)
        {
          Object[] res = dialog.getResult();
          if (res.length == 1) {
            result.setText(((Path)res[0]).toString());
          }
        }
      }
    });
    return result;
  }
  
  public static Text createLabelText(Composite container, String text, ModifyListener listener)
  {
    Label label = new Label(container, 0);
    label.setText(text);
    Text result = new Text(container, 2052);
    GridData gd = new GridData(768);
    result.setLayoutData(gd);
    if (listener != null) {
      result.addModifyListener(listener);
    }
    return result;
  }
  
  public static GridData createGridData()
  {
    return new GridData(4, 128, true, false);
  }
}

