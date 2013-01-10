package cucumber.eclipse.editor.editors;

import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class GherkinFormattingStrategy extends ContextBasedFormattingStrategy  {


	/** Documents to be formatted by this strategy */
	private final LinkedList<IDocument> fDocuments= new LinkedList<IDocument>();
	/** Partitions to be formatted by this strategy */
	private final LinkedList<TypedPosition> fPartitions= new LinkedList<TypedPosition>();


	public GherkinFormattingStrategy() {
		super();
	}


	@Override
	public void format() {
		super.format();

		final IDocument document= fDocuments.removeFirst();
		final TypedPosition partition= fPartitions.removeFirst();

		if (document != null && partition != null) {
			Map<String, IDocumentPartitioner> partitioners= null;
			try {

				final TextEdit edit = GherkinFormatterUtil.formatTextEdit(document.get(), 0, "\n");
				if (edit != null) {
					edit.apply(document);
				}

			} catch (MalformedTreeException exception) {
				//JavaPlugin.log(exception);
			} catch (BadLocationException exception) {
				//JavaPlugin.log(exception);
			} finally {
				if (partitioners != null) 
					;//TextUtilities.addDocumentPartitioners(document, partitioners);
			}
		}
 	}


	@Override
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);

		fPartitions.addLast((TypedPosition) context.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
		fDocuments.addLast((IDocument) context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
	}


	@Override
	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}
}
