package ch.eth.scu.importer.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public abstract class CustomTreeNode extends DefaultMutableTreeNode{ 

	private static final long serialVersionUID = 1L;

	private String type;
	
	public CustomTreeNode(Object object, String type) {
		super(object);
		this.type = type;
	}

	public abstract javax.swing.Icon getIcon();
	
	public String getType() { return type; }

	public String getTooltip() { return ""; } 
	
}