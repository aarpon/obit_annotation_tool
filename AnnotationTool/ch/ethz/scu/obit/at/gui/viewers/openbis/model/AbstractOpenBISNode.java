package ch.ethz.scu.obit.at.gui.viewers.openbis.model;

import javax.swing.tree.DefaultMutableTreeNode;

import ch.ethz.scu.obit.at.interfaces.ILazyNode;

/**
 * Customized Node to be used in a JTree that renders the content of the
 * openBIS server.
 * @author Aaron Ponti 
 */
public abstract class AbstractOpenBISNode extends DefaultMutableTreeNode
	implements ILazyNode {

	private static final long serialVersionUID = 1L;
	
	protected String identifier;

	protected boolean isLoaded = false;
	
	/**
	 * Constructor
	 * @param object User object to be stored
	 */
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
	 * @return string representation of the Node
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
	 * Each specialization of this class must specify if it is a leaf 
	 * (i.e. it cannot be expanded) or not: this is essential for lazy 
	 * loading
	 */
	public abstract boolean isLeaf();
	
	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */	
	public String getTooltip() { return ""; }

	/**
	 * Return true if the node children were (lazy) loaded already
	 * @return true if the children were loaded, false otherwise
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * Indicate that the children of current node were (lazy-)loaded
	 */
	public void setLoaded() {
		isLoaded = true;
	}
	
}
