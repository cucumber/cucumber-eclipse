package io.cucumber.eclipse.java.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.filtertable.Filter;
import org.eclipse.jdt.internal.ui.filtertable.FilterLabelProvider;
import org.eclipse.jdt.internal.ui.filtertable.FilterViewerComparator;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

import io.cucumber.eclipse.java.CucumberJavaUIMessages;

public abstract class GlueCodePackageTable {

	/**
	 * Content provider for the table. Content consists of instances of StepFilter.
	 * 
	 */
	class StepDefinitionsFilterContentProvider implements IStructuredContentProvider {
		public StepDefinitionsFilterContentProvider() {
			initTableState(false);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getAllFiltersFromTable();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}

//	private Button fUseStepDefinitionsFiltersButton;
	private Button fAddPackageButton;
//	private Button fAddTypeButton;
	private Button fRemoveFilterButton;
//	private Button fAddFilterButton;
	private Button fSelectAllButton;
	private Button fDeselectAllButton;

	private CheckboxTableViewer fTableViewer;
	private Group group;

	public GlueCodePackageTable(Composite container) {
		group = new Group(container, SWT.NONE);
		group.setText(" Cucumber Glue Packages ");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTableViewer = CheckboxTableViewer.newCheckList(group,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fTableViewer.getTable().setFont(container.getFont());
		fTableViewer.setLabelProvider(new FilterLabelProvider());
		fTableViewer.setComparator(new FilterViewerComparator());
		fTableViewer.setContentProvider(new StepDefinitionsFilterContentProvider());
		fTableViewer.setInput(getAllStoredFilters(false));
		fTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				((Filter) event.getElement()).setChecked(event.getChecked());
			}
		});
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					fRemoveFilterButton.setEnabled(false);
				} else {
					fRemoveFilterButton.setEnabled(true);
				}
			}
		});
		fTableViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				handleFilterViewerKeyPress(event);
			}
		});

		createStepFilterButtons(group);
	}

	/**
	 * handles the filter button being clicked
	 * 
	 * @param event the clicked event
	 */
	private void handleFilterViewerKeyPress(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			removeFilters();
		}
	}

	/**
	 * initializes the checked state of the filters when the dialog opens
	 * 
	 * @since 3.2
	 */
	private void initTableState(boolean defaults) {
		Filter[] filters = getAllStoredFilters(defaults);
		for (int i = 0; i < filters.length; i++) {
			fTableViewer.add(filters[i]);
			fTableViewer.setChecked(filters[i], filters[i].isChecked());
		}
	}

	/**
	 * adds a single filter to the viewer
	 * 
	 * @param filter  the new filter to add
	 * @param checked the checked state of the new filter
	 * @since 3.2
	 */
	protected void addFilter(String filter, boolean checked) {
		if (filter != null) {
			Filter f = new Filter(filter, checked);
			fTableViewer.add(f);
			fTableViewer.setChecked(f, checked);
		}
	}

	/**
	 * returns all of the filters from the table, this includes ones that have not
	 * yet been saved
	 * 
	 * @return a possibly empty lits of filters fron the table
	 * @since 3.2
	 */
	protected Filter[] getAllFiltersFromTable() {
		TableItem[] items = fTableViewer.getTable().getItems();
		Filter[] filters = new Filter[items.length];
		for (int i = 0; i < items.length; i++) {
			filters[i] = (Filter) items[i].getData();
			filters[i].setChecked(items[i].getChecked());
		}
		return filters;
	}

	public FilterStrings getFilters() {
		ArrayList<String> active = new ArrayList<String>();
		ArrayList<String> inactive = new ArrayList<String>();
		String name = ""; //$NON-NLS-1$
		Filter[] filters = getAllFiltersFromTable();
		for (int i = 0; i < filters.length; i++) {
			name = filters[i].getName();
			if (filters[i].isChecked()) {
				active.add(name);
			} else {
				inactive.add(name);
			}
		}
		return new FilterStrings( CucumberJavaPreferences.serializeList(active.toArray(new String[active.size()])), CucumberJavaPreferences.serializeList(inactive.toArray(new String[inactive.size()])));
	}

	public void performDefaults() {
		fTableViewer.getTable().removeAll();
		initTableState(true);
	}

	/**
	 * Returns all of the committed filters
	 * 
	 * @param defaults when true defaults values are used
	 * @return an array of committed filters
	 * @since 3.2
	 */
	protected Filter[] getAllStoredFilters(boolean defaults) {
		List<String> activefilters = CucumberJavaPreferences.parseList(getFilter(true, defaults));
		List<String> inactivefilters = CucumberJavaPreferences.parseList(getFilter(false, defaults));
		Filter[] filters = new Filter[activefilters.size() + inactivefilters.size()];
		for (int i = 0; i < activefilters.size(); i++) {
			filters[i] = new Filter(activefilters.get(i), true);
		}
		for (int i = 0; i < inactivefilters.size(); i++) {
			filters[i + activefilters.size()] = new Filter(inactivefilters.get(i), false);
		}
		return filters;
	}

	protected abstract String getFilter(boolean active, boolean defaults);

	/**
	 * Allows a new filter to be added to the listing
	 */
	private void addFilter() {
		Filter newfilter = CreateStepDefinitionsFilterDialog
				.showCreateStepFilterDialog(getShell(),
				getAllFiltersFromTable());
		if (newfilter != null) {
			fTableViewer.add(newfilter);
			fTableViewer.setChecked(newfilter, true);
			fTableViewer.refresh(newfilter);
		}
	}

	/**
	 * Creates the button for the step filter options
	 * 
	 * @param container the parent container
	 */
	private void createStepFilterButtons(Composite container) {
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);
		// Add filter button
//		fAddFilterButton = SWTFactory.createPushButton(buttonContainer,
//				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add_filter,
//				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Key_in_the_name_of_a_new_step_filter, null);
//		fAddFilterButton.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event e) {
//				addFilter();
//			}
//		});
//		// Add type button
//		fAddTypeButton = SWTFactory.createPushButton(buttonContainer,
//				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add_Type,
//				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Choose_a_Java_type_and_add_it_to_step_definitions_filters,
//				null);
//		fAddTypeButton.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event e) {
//				addType();
//			}
//		});
		// Add package button
		fAddPackageButton = SWTFactory.createPushButton(buttonContainer,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add__Package,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Choose_a_package_and_add_it_to_step_definitions_filters,
				null);
		fAddPackageButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				addPackage();
			}
		});
		// Remove button
		fRemoveFilterButton = SWTFactory.createPushButton(buttonContainer,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage___Remove,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Remove_all_selected_step_filters, null);
		fRemoveFilterButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				removeFilters();
			}
		});
		fRemoveFilterButton.setEnabled(false);

		Label separator = new Label(buttonContainer, SWT.NONE);
		separator.setVisible(false);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		// Select All button
		fSelectAllButton = SWTFactory.createPushButton(buttonContainer,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Select_All,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Selects_all_step_filters, null);
		fSelectAllButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				fTableViewer.setAllChecked(true);
			}
		});
		// De-Select All button
		fDeselectAllButton = SWTFactory.createPushButton(buttonContainer,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Deselect_All,
				CucumberJavaUIMessages.CucumberJavaPreferencesPage__Deselects_all_step_filters, null);
		fDeselectAllButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				fTableViewer.setAllChecked(false);
			}
		});

	}
	


	/**
	 * add a new type to the listing of available filters
	 */
	private void addType() {
		try {
			SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), PlatformUI.getWorkbench().getProgressService(),
					SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
			dialog.setTitle(CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add_type_to_step_filters);
			dialog.setMessage(
					CucumberJavaUIMessages.CucumberJavaPreferencesPage__Select_a_type_to_filter_when_stepping);
			if (dialog.open() == IDialogConstants.OK_ID) {
				Object[] types = dialog.getResult();
				if (types != null && types.length > 0) {
					IType type = (IType) types[0];
					addFilter(type.getFullyQualifiedName(), true);
				}
			}
		} catch (JavaModelException jme) {
			ExceptionHandler.handle(jme, CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add_type_to_step_filters,
					CucumberJavaUIMessages.CucumberJavaPreferencesPage__Could_not_open_type_selection_dialog_for_step_filters);
		}
	}

	private Shell getShell() {
		return fTableViewer.getControl().getShell();
	}

	/**
	 * add a new package to the list of all available package filters
	 */
	private void addPackage() {
		try {
			ElementListSelectionDialog dialog = JDIDebugUIPlugin.createAllPackagesDialog(getShell(), null, false);
			dialog.setTitle(CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add_package_to_step_filters);
			dialog.setMessage(
					CucumberJavaUIMessages.CucumberJavaPreferencesPage__Select_a_package_to_filter_when_stepping);
			dialog.setMultipleSelection(true);
			if (dialog.open() == IDialogConstants.OK_ID) {
				Object[] packages = dialog.getResult();
				if (packages != null) {
					IJavaElement pkg = null;
					for (int i = 0; i < packages.length; i++) {
						pkg = (IJavaElement) packages[i];
						String filter = pkg.getElementName() + ".*"; //$NON-NLS-1$
						addFilter(filter, true);
					}
				}
			}

		} catch (JavaModelException jme) {
			ExceptionHandler.handle(jme,
					CucumberJavaUIMessages.CucumberJavaPreferencesPage__Add_package_to_step_filters,
					CucumberJavaUIMessages.CucumberJavaPreferencesPage__Could_not_open_package_selection_dialog_for_step_filters);
		}
	}

	/**
	 * Removes the currently selected filters.
	 */
	protected void removeFilters() {
		fTableViewer.remove(((IStructuredSelection) fTableViewer.getSelection()).toArray());
	}

	public static record FilterStrings(String active, String inactive) {

	}

	public void setEnabled(boolean enable) {
		fTableViewer.getControl().setEnabled(enable);
		fAddPackageButton.setEnabled(enable);
		fDeselectAllButton.setEnabled(enable);
		fRemoveFilterButton.setEnabled(enable);
		fSelectAllButton.setEnabled(enable);
		group.setEnabled(enable);
	}

	public Group getControl() {
		return group;
	}
}
