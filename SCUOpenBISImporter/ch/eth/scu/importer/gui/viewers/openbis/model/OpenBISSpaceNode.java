package ch.eth.scu.importer.gui.viewers.openbis.model;

import ch.eth.scu.importer.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

/**
 * A Custom Space TreeNode
 * @author Aaron Ponti
 *
 */
public class OpenBISSpaceNode extends AbstractOpenBISNode {

	private static final long serialVersionUID = 1L;
	
	private SpaceWithProjectsAndRoleAssignments s;
	
	/**
	 * Constructor 
	 * @param s SpaceWithProjectsAndRoleAssignments object
	 */
	public OpenBISSpaceNode(SpaceWithProjectsAndRoleAssignments s) {
		super(s);
		this.s = s;
	}
	
	/**
	 * Returns the type of the node
	 * @return Type of the node
	 */		
	@Override
	public String getType() { return "Space"; }

	/**
	 * String representation of the node
	 * @return String representation of the node
	 */		
	@Override
	public String toString() { return s.getCode(); }

	/**
	 * Space code
	 * @return Space code
	 */			
	@Override
	public String getCode() { return s.getCode(); }
	
	/**
	 * Space identifier
	 * @return Space identifier
	 */			
	@Override
	public String getIdentifier() { return s.getCode(); }

	/**
	 * Get the icon to be displayed in the JTree
	 * @return the icon to be displayed in the JTree
	 */	
	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("./icons/space.png"));
	}
	
	/**
	 * Get the tooltip to be displayed when the user places the mouse 
	 * over the Node in the JTree
	 * @return the tooltip to be displayed over the Node
	 */	
	public String getTooltip() { return "openBIS space"; }

}
