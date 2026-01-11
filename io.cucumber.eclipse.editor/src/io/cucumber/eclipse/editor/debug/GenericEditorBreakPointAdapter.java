package io.cucumber.eclipse.editor.debug;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextViewer;
import org.osgi.service.component.annotations.Component;

import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;

/**
 * Workaround for Bug 575970
 * 
 * @author christoph
 *
 */
@Component(service = IAdapterFactory.class, property = {
		IAdapterFactory.SERVICE_PROPERTY_ADAPTABLE_CLASS
				+ "=org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor",
		IAdapterFactory.SERVICE_PROPERTY_ADAPTER_NAMES + "=org.eclipse.debug.ui.actions.IToggleBreakpointsTarget" })
public class GenericEditorBreakPointAdapter implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IToggleBreakpointsTarget.class) {
			ITextViewer textViewer = Adapters.adapt(adaptableObject, ITextViewer.class);
			if (textViewer != null) {
				GherkinEditorDocument gherkinEditorDocument = GherkinEditorDocumentManager.get(textViewer.getDocument());
				if (gherkinEditorDocument != null) {
					return adapterType.cast(new GherkingToggleBreakpointsTarget());
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IToggleBreakpointsTarget.class };
	}

}
