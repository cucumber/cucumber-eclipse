package io.cucumber.eclipse.editor.contentassist;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Displays HTML data in an Informationcontrol.
 * 
 * @author christoph
 *
 */
public class HtmlInformationControl extends AbstractInformationControl {

	private String html;

	public HtmlInformationControl(Shell shell, String html) {
		super(shell, true);
		this.html = html;
		create();
	}

	@Override
	public boolean hasContents() {
		return true;
	}

	@Override
	protected void createContent(Composite parent) {
		Browser browser = new Browser(parent, SWT.NONE);
		browser.setBackground(parent.getBackground());
		browser.setForeground(parent.getForeground());
		browser.setText(html, true);
	}
}
