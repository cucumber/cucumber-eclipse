package io.cucumber.eclipse.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Access icons/images in a semantic fashion
 * 
 * @author christoph
 *
 */
public class Images {

	public static Image getCukesIcon() {
		return Activator.getImage(Activator.ICON_CUKES);
	}

	public static ImageDescriptor getCukesIconDescriptor() {
		return Activator.getImageDescriptor(Activator.ICON_CUKES);
	}
}
