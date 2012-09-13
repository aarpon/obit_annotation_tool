package ch.eth.scu.importer.gui.viewers.openbis.model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Customized Node to be used in a JTree that renders the content of the
 * openBIS server.
 * @author Aaron Ponti 
 */
public abstract class AbstractOpenBISNode extends DefaultMutableTreeNode {

	protected String identifier;
	
	private static final long serialVersionUID = 1L;

	public AbstractOpenBISNode(Object object) {
		super(object);
	}

	/**
	 * Get the type of the Node
	 * @return type of the Node
	 */
	abstract public String getType();

	/**
	 * String representation of the Node
	 * @return string representarion of the Node
	 */
	abstract public String toString();

	/**
	 * Get the openBIS code associated with the element
	 * @return the openBIS code
	 */
	abstract public String getCode();

	/**
	 * Get the openBIS identifier associated with the element
	 * @return the openBIS identifier
	 */	
	abstract public String getIdentifier();

	/**
	 * Get the icon to be displayed in the JTree
	 * @return the icon to be displayed in the JTree
	 */	
	public abstract javax.swing.Icon getIcon();
	
	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */	
	public String getTooltip() { return ""; }

}
