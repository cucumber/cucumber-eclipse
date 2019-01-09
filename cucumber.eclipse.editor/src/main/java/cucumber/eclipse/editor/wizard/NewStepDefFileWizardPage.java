package cucumber.eclipse.editor.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
//import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import cucumber.eclipse.editor.util.JDTUtil;
import cucumber.eclipse.editor.util.SWTUtil;
import cucumber.eclipse.editor.util.Utils;

/**
 * @author girija.panda@nokia.com
 * 
 * Purpose: For developing a New File Wizard page for Step definition(java) file
 * User can create a new  Step definition(java) file from Eclipse Menu : 
 * File &gt; New &gt; Other &gt; Cucumber &gt; Step-Definition class
 * 
 */
public class NewStepDefFileWizardPage extends WizardPage {

	private Text m_sourceFolderText;
	private Text m_packageNameText;
	private Text m_featureNameText;

	private static final String PAGE_TITLE = "New Cucumber Step Definition File";
	private static final String PAGE_DESC = "Specify additional information about Step Definition File";

	//private static final String FILE_ALREADY_EXIST = "Step Definition File already exists";
	//private static final String FILE_OVERWRITE = "Step Definition File {0} already exists. Overwrite it?";

	private List<Utils.JavaElement> m_elements = null;

	//private Map<String, Button> m_keywords = new HashMap<String, Button>();
	//public static final String[] KEYWORDS = { "Feature","Scenario","", "Given","When","Then", "And","But","", "Scenario Outline","Examples","Background" };

	//For Annotations
	private Map<String, Button> m_annotations = new HashMap<String, Button>();
	public static final String[] ANNOTATIONS = {"Given","When","Then", "And","But","", };

	public NewStepDefFileWizardPage() {
		super(PAGE_TITLE);
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESC);
	}

	
	@Override
	public void createControl(Composite parent) {
		
		//System.out.println("NewStepDefFileWizardPage:createControl() Inside....");
		Composite container = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		createTop(container);
		createBottom(container);

		initialize();
		dialogChanged();
		setControl(container);
		//System.out.println("NewStepDefFileWizardPage:createControl() END....");
	}

	// createTop
	private void createTop(Composite parent) 
	{
		//System.out.println("NewFeaturFileWizardPage:createTop() Inside....");
		final Composite container = SWTUtil.createGridContainer(parent, 3);	
		this.m_sourceFolderText = SWTUtil.createPathBrowserText(container,
				"&Source Folder:", new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						NewStepDefFileWizardPage.this.dialogChanged();
					}
				
		});
		
		Label label = new Label(container, 0);
		label.setText("&Package Name:");
		this.m_packageNameText = new Text(container, 2052);
		this.m_packageNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				NewStepDefFileWizardPage.this.dialogChanged();
			}
		});
		
		GridData gd = new GridData(768);
		this.m_packageNameText.setLayoutData(gd);
		Button button = new Button(container, 8);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				NewStepDefFileWizardPage.this.handleBrowsePackages(container
						.getShell());
			}
		});

		Label label1 = new Label(container, 0);
		label1.setText("&Class Name:");
		this.m_featureNameText = new Text(container, 2052);
		GridData gd1 = new GridData(768);
		this.m_featureNameText.setLayoutData(gd1);
		this.m_featureNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				NewStepDefFileWizardPage.this.dialogChanged();
			}
		});
		//System.out.println("NewFeaturFileWizardPage:createTop() END....");
	}

	// handleBrowsePackages
	private void handleBrowsePackages(Shell dialogParrentShell) {
		//System.out.println("NewFeaturFileWizardPage:handleBrowsePackages() Inside....");
		try 
		{
			IResource sourceContainer = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getSourceFolder()));
			IJavaProject javaProject = JDTUtil.getJavaProject(sourceContainer.getProject().getName());
			SelectionDialog dialog = JavaUI.createPackageDialog(dialogParrentShell, javaProject, 0);
			dialog.setTitle("Package Selection");
			dialog.setMessage("&Choose A Package:");
			if (dialog.open() == 0) {
				Object[] selectedPackages = dialog.getResult();
				if (selectedPackages.length == 1) {
					this.m_packageNameText.setText(((IPackageFragment) selectedPackages[0]).getElementName());
				}
			}
		} catch (JavaModelException localJavaModelException) {
			updateStatus("Failed To List Packages.");
		}
		//System.out.println("NewFeaturFileWizardPage:handleBrowsePackages() END....");
	}

	// initialize
	private void initialize() {
			//System.out.println("NewFeaturFileWizardPage:initialize() Inside....");
			this.m_elements = Utils.getSelectedJavaElements();
			if (this.m_elements.size() > 0) 
			{
				Utils.JavaElement sel = (Utils.JavaElement) this.m_elements.get(0);
				
				if (sel.sourceFolder != null) {
					this.m_sourceFolderText.setText(sel.sourceFolder);
				}
				
				if (sel.getPackageName() != null) {
					this.m_packageNameText.setText(sel.getPackageName());
				}
				
				if(sel.getClassName() != null ){
					this.m_featureNameText.setText(sel.getClassName());
				}
				
			}
			//System.out.println("NewFeaturFileWizardPage:initialize() END....");
		}

	
		public List<Utils.JavaElement> getJavaElements() {
			return this.m_elements;
		}

	// For Dialog Changed
	private void dialogChanged() {
		//System.out.println("NewFeaturFileWizardPage:dialogChanged() Inside....");
		IResource container = null;
		IProject project = null;		
		String className = "";
		String projectName = "";
		String sourceText = "";
		
		//MUST To Avoid NullPointerException
		try
		{
			className = getClassName();
			container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getSourceFolder()));		
			project = container.getProject(); //To avoid NullPointerException			
			sourceText = this.m_sourceFolderText.getText();			
			//System.out.println("Project-1 :" +project);
			//System.out.println("ProjectName-1 :" +projectName);			
			//System.out.println("sourceText-1 :" +sourceText);	
			if(project != null){
				projectName = project.getName();
				//System.out.println("ProjectName-2 :" +projectName);
			}			
			if(project == null)
			{
				if("".equals(projectName) || projectName.length()==0)
				{		
					updateStatus("The Source Folder of an existing project must be specified.");		
					return;
				}
			}
			if (!(sourceText.endsWith("/src/main/java") 
					|| sourceText.endsWith("/src/test/java")) )
			{
				//System.out.println("ProjectTextPath:" +sourceText);
				updateStatus("Source Folder Must Contain '/src/main/java' or '/src/test/java'");		
				return;
			}
			
		}catch(Exception ex){
			updateStatus("Source Folder is not a Java project.");		
			return;
		}
		if (getPackageName().length() == 0) {
			updateStatus("The Package Must Be Specified");
			return;
		}
		if ((container != null) && (!container.isAccessible())) {
			updateStatus("Project Must Be Writable");
			return;
		}
		if (className.length() == 0) {
			updateStatus("Class Name Must Be Specified");
			return;
		}
		if (className.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("Class Name Must Be Valid");
			return;
		}
		int dotLoc = className.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = className.substring(dotLoc + 1);
			/*if (!ext.equalsIgnoreCase("feature")) {
				updateStatus("File extension must be \"feature\"");
				return;
			}*/
			if (!ext.equalsIgnoreCase("java")) {
				updateStatus("File extension must be \"java\"");
				return;
			}
		}
		updateStatus(null);
		//System.out.println("NewFeaturFileWizardPage:dialogChanged() END....");
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	
	
	private void createBottom(Composite parent) {		
		//System.out.println("NewFeaturFileWizardPage:createBottom() Inside....");
		Group g = new Group(parent, 64);
		g.setText("Cucumber Annotations");
		
		GridData gd = new GridData(768);
		g.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		g.setLayout(layout);
		layout.numColumns = 3;
		
		for (String label : ANNOTATIONS) {
			if ("".equals(label)) {
				new Label(g, 0);
			} 
			else{
					Button b = new Button(g, "".equals(label) ? 0 : 32);
					this.m_annotations.put(label, b);
					b.setText("@" + label);
			}
		}
	}

	public String getSourceFolder() {
		return this.m_sourceFolderText.getText();
	}

	public String getPackageName() {
		return this.m_packageNameText.getText();
	}

	public String getClassName() {
		return this.m_featureNameText.getText();
	}

	public boolean containsAnnotation(String annotation) {
		Button b = (Button) this.m_annotations.get(annotation);
		return b.getSelection();
	}

	public String getPackage() {
		return this.m_packageNameText.getText();
	}

}
