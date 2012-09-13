package ch.eth.scu.importer.gui.viewers.openbis.model;

import javax.swing.Icon;


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
				getClass().getResource("./icons/user.png"));
	}

	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */
	@Override
	public String getTooltip() {
		return "Shows the user name when logged in.";
	}

}
