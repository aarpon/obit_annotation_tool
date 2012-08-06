package ch.eth.scu.importer.gui.components.viewers;

import ch.eth.scu.importer.gui.descriptors.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class SubImageNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public SubImageNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("subimage.png"));
	}
	
	public String getTooltip() {
		return "Sub-image";
	}
}