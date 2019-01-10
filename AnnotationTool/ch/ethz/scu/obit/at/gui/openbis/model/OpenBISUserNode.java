package ch.ethz.scu.obit.at.gui.openbis.model;

import javax.swing.Icon;


/**
 * @author Aaron Ponti
 * openBIS User node.
 */
public class OpenBISUserNode extends AbstractOpenBISNode {

	private static final long serialVersionUID = 1L;
	
	protected String name = "";
	
	/**
	 * Constructor
	 * @param username User name
	 */
	public OpenBISUserNode(String username) {
		super(username);
		this.name = username;
	}
	
	/**
	 * Returns the type of the node
	 * @return Type of the node
	 */
	public String getType() { return "User"; }

	/**
	 * String representation of the node
	 * @return String representation of the node
	 */
	@Override
	public String toString() { return name; }

	/**
	 * Root code (custom)
	 * @return String openBIS code of the root object (set to "ROOT")
	 */
	@Override
	public String getCode() { return "USER"; }

	/**
	 * User identifier (custom)
	 * @return String openBIS identifier of the user object (set to "/")
	 */
	@Override
	public String getIdentifier() { return "/"; }

	/**
	 * Get the icon to be displayed in the JTree
	 * @return the icon to be displayed in the JTree
	 */		
	@Override
	public Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/user.png"));
	}

	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */
	@Override
	public String getTooltip() {
		return "The virtual root of openBIS with all spaces you have access to.";
	}

	/**
	 * User has children and is therefore not a leaf
	 * @return false
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}
	
}
