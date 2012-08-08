package ch.eth.scu.importer.gui.viewers.model;

import javax.swing.tree.DefaultMutableTreeNode;

import ch.eth.scu.importer.processor.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public abstract class CustomTreeNode extends DefaultMutableTreeNode{ 

	private static final long serialVersionUID = 1L;

	protected String type;
	
	public CustomTreeNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public abstract javax.swing.Icon getIcon();
	
	public String getType() { return type; }

	public String getTooltip() { return ""; }
	
}