package ch.eth.scu.importer.gui.viewers.data.model;

import ch.eth.scu.importer.processor.model.RootDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class RootNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public RootNode(RootDescriptor object) {
		super(object);
		this.type = "root";
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("root.png"));
	}
}