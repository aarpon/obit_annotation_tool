package ch.eth.scu.importer.gui.viewers.model;

import ch.eth.scu.importer.processor.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class FCSFileNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public FCSFileNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("fcs.png"));
	}
	
	public String getTooltip() {
		return "FCS file";
	}
}