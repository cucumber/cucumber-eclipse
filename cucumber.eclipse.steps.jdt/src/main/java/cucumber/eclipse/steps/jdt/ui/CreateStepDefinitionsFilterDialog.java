package cucumber.eclipse.steps.jdt.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.actions.StatusInfo;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.Filter;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateStepDefinitionsFilterDialog extends StatusDialog {

	private static final String DEFAULT_NEW_FILTER_TEXT = ""; //$NON-NLS-1$

	private Text text;
	private Filter filter;
	private Button okButton;

	private boolean filterValid;
	private boolean okClicked;
	private Filter[] existingFilters;

	private CreateStepDefinitionsFilterDialog(Shell parent, Filter filter, Filter[] existingFilters) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.filter = filter;
		this.existingFilters = existingFilters;

		setTitle(CucumberJavaUIMessages.CreateStepDefinitionsFilterDialog);
		setStatusLineAboveButtons(false);

	}

	static Filter showCreateStepFilterDialog(Shell parent, Filter[] existingFilters) {
		CreateStepDefinitionsFilterDialog createStepFilterDialog = new CreateStepDefinitionsFilterDialog(parent,
				new Filter(DEFAULT_NEW_FILTER_TEXT, true), existingFilters);
		createStepFilterDialog.create();
		createStepFilterDialog.open();

		return createStepFilterDialog.filter;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 15;
		gridLayout.marginWidth = 15;
		container.setLayout(gridLayout);

		int textStyles = SWT.SINGLE | SWT.LEFT;
		Label label = new Label(container, textStyles);
		label.setText(CucumberJavaUIMessages.CreateStepDefinitionsFilterDialog);
		label.setFont(container.getFont());

		// create & configure Text widget for editor
		// Fix for bug 1766. Border behavior on for text fields varies per platform.
		// On Motif, you always get a border, on other platforms,
		// you don't. Specifying a border on Motif results in the characters
		// getting pushed down so that only there very tops are visible. Thus,
		// we have to specify different style constants for the different platforms.
		if (!SWT.getPlatform().equals("motif")) { //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}

		text = new Text(container, textStyles);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 300;
		text.setLayoutData(gridData);
		text.setFont(container.getFont());

		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateChange();
				if (!filterValid) {
					updateStatus(new StatusInfo(IStatus.ERROR, CucumberJavaUIMessages.CreateStepDefinitionsFilterDialog_4));
				} else if (isDuplicateFilter(text.getText().trim())) {
					updateStatus(new StatusInfo(IStatus.WARNING, CucumberJavaUIMessages.CreateStepDefinitionsFilterDialog_5));
					return;
				} else {
					updateStatus(new StatusInfo());
				}
			}
		});

		return container;
	}

	private void validateChange() {
		String trimmedValue = text.getText().trim();

		if (trimmedValue.length() > 0 && validateInput(trimmedValue)) {
			okButton.setEnabled(true);
			filter.setName(text.getText());
			filterValid = true;
		} else {
			okButton.setEnabled(false);
			filter.setName(DEFAULT_NEW_FILTER_TEXT);
			filterValid = false;
		}
	}

	private boolean isDuplicateFilter(String trimmedValue) {
		for (int i = 0; i < existingFilters.length; i++) {
			if (existingFilters[i].getName().equals(trimmedValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A valid step filter is simply one that is a valid Java identifier. and, as
	 * defined in the JDI spec, the regular expressions used for step filtering must
	 * be limited to exact matches or patterns that begin with '*' or end with '*'.
	 * Beyond this, a string cannot be validated as corresponding to an existing
	 * type or package (and this is probably not even desirable).
	 */
	private boolean validateInput(String trimmedValue) {
		char firstChar = trimmedValue.charAt(0);
		if (!Character.isJavaIdentifierStart(firstChar)) {
			if (!(firstChar == '*')) {
				return false;
			}
		}
		int length = trimmedValue.length();
		for (int i = 1; i < length; i++) {
			char c = trimmedValue.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '.' && i != (length - 1)) {
					continue;
				}
				if (c == '*' && i == (length - 1)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the name of the section that this dialog stores its settings in
	 *
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IJavaDebugUIConstants.PLUGIN_ID + ".CREATE_STEP_FILTER_DIALOG_SECTION"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	@Override
	public boolean close() {
		if (!okClicked) {
			filterValid = false;
			filter = null;
		}
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsSectionName());
		}
		return section;
	}

	@Override
	protected void okPressed() {
		okClicked = true;
		super.okPressed();
	}
}
