package ch.eth.scu.importer.bdfacsdivafcs.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.data.model.PathAwareDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class XMLFileNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public XMLFileNode(PathAwareDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/xml.png"));
	}
	
	public String getTooltip() {
		return "XML file";
	}
}