package cucumber.eclipse.editor.editors;

import gherkin.formatter.model.BasicStatement;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class GherkinOutlinePage extends ContentOutlinePage {

	private class ContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return featureElement == null ? new Object[0]
					: new Object[] { featureElement };
		}

		public Object[] getChildren(Object parentElement) {
			return ((PositionedElement) parentElement).getChildren().toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return !((PositionedElement) element).getChildren().isEmpty();
		}
	}

	private class LabelProvider extends BaseLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			return element.toString();
		}
	}

	private static class ElementComparer implements IElementComparer {
		public int hashCode(Object element) {
			return element instanceof PositionedElement
					? ((PositionedElement) element).getStatement().getLine()
							: element.hashCode();
		}
	
		public boolean equals(Object a, Object b) {
			if (a instanceof PositionedElement && b instanceof PositionedElement) {
				BasicStatement s1 = ((PositionedElement) a).getStatement();
				BasicStatement s2 = ((PositionedElement) b).getStatement();
				
				return s1.getLine().equals(s2.getLine())
						&& s1.getKeyword().equals(s2.getKeyword())
						&& s1.getName().equals(s2.getName());
			}
			
			return a.equals(b);
		}
	}

	private PositionedElement featureElement;

	private TreeViewer viewer;

	private IEditorInput input;

	public void createControl(Composite parent) {
		super.createControl(parent);
		viewer = getTreeViewer();
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setComparer(new ElementComparer());
		viewer.setInput(input);
	}

	public void setInput(IEditorInput input) {
		this.input = input;
		if (viewer != null) {
			viewer.setInput(input);
		}
	}

	public void update(PositionedElement featureElement) {
		this.featureElement = featureElement;
		if (viewer != null) {
			viewer.refresh();
		}
	}
}
