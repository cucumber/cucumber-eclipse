package cucumber.eclipse.editor.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class GherkinDocumentProvider extends FileDocumentProvider {

	private String sampleFeatureContent = null;

	
	//Initialize Sample Feature Content
	public GherkinDocumentProvider(String sampleFeatureContent){
		this.sampleFeatureContent = sampleFeatureContent;
	}
	
	
	protected IDocument createDocument(Object element) throws CoreException {
		
		IDocument document = super.createDocument(element);
		
		if (document != null) {
			
			// By Girija 
			// Check If Feature File Editor content is not Empty
			// Then override with Sample Content
			if(document.get() == null || document.getLength() == 0)
			{
				document.set(sampleFeatureContent);
			}
			
			IDocumentPartitioner partitioner =
				new GherkinPartitioner(
					new RuleBasedPartitionScanner(),
					new String[] { IDocument.DEFAULT_CONTENT_TYPE });
			partitioner.connect(document);
			
			document.setDocumentPartitioner(partitioner);
		}
		
		
		return document;
	}
}