package io.cucumber.eclipse.editor.compare;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

/**
 * Node representing a structural element in a Gherkin document for compare operations.
 * <p>
 * This class represents nodes in the compare tree view, such as Features, Scenarios, Steps, etc.
 * Each node corresponds to a specific range in the document and has a type that determines
 * its icon and behavior in the compare view.
 * </p>
 * <p>
 * Extends DocumentRangeNode to properly integrate with Eclipse Compare framework for
 * navigation and content display.
 * </p>
 */
public class GherkinNode extends DocumentRangeNode implements ITypedElement {

	// Node types
	public static final String FEATURE_FILE = "feature-file";
	public static final String FEATURE = "feature";
	public static final String SCENARIO = "scenario";
	public static final String BACKGROUND = "background";
	public static final String RULE = "rule";
	public static final String STEP = "step";
	public static final String EXAMPLES = "examples";

	private final String nodeType;

	/**
	 * Creates a new Gherkin node
	 * 
	 * @param parent     parent node (null for root)
	 * @param typeCode   type code for this node (used by Eclipse Compare)
	 * @param id         unique identifier for this node
	 * @param document   the document containing this node
	 * @param start      start offset in document
	 * @param length     length of the node's range
	 * @param nodeType   the Gherkin element type (feature, scenario, etc.)
	 */
	public GherkinNode(DocumentRangeNode parent, int typeCode, String id, IDocument document, 
			int start, int length, String nodeType) {
		super(parent, typeCode, id, document, start, length);
		this.nodeType = nodeType;
	}

	@Override
	public String getName() {
		return getId();
	}

	@Override
	public String getType() {
		return nodeType;
	}

	@Override
	public Image getImage() {
		// Return null to use default icons
		return null;
	}

	/**
	 * Get the node type
	 */
	public String getNodeType() {
		return nodeType;
	}
}