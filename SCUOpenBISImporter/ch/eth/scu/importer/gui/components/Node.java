package ch.eth.scu.importer.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public abstract class Node extends DefaultMutableTreeNode{ 

	private static final long serialVersionUID = 1L;

	public Node(Object object) {
		super(object);
	}

	public abstract javax.swing.Icon getIcon();

	public String getTooltip() { return ""; } 
}