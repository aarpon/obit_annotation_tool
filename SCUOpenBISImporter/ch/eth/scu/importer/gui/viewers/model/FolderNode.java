package ch.eth.scu.importer.gui.viewers.model;

import ch.eth.scu.importer.processor.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class FolderNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public FolderNode(AbstractDescriptor object) {
		super(object);
		this.type = "root";
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("folder.png"));
	}
}