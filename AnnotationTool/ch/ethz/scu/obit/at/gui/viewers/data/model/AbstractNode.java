package ch.ethz.scu.obit.at.gui.viewers.data.model;

import javax.swing.tree.DefaultMutableTreeNode;

import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree that renders the hierarchical
 * experiment content of datasets
 * @author Aaron Ponti 
 */
public abstract class AbstractNode extends DefaultMutableTreeNode { 

	private static final long serialVersionUID = 1L;

	protected String type;
	
	public AbstractNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public abstract javax.swing.Icon getIcon();
	
	public String getType() { return type; }

	public String getTooltip() { return ""; }
	
}