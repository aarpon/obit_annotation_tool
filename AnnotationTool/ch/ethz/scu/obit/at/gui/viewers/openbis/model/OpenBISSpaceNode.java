package ch.ethz.scu.obit.at.gui.viewers.openbis.model;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;

/**
 * A Custom Space TreeNode
 * @author Aaron Ponti
 *
 */
public class OpenBISSpaceNode extends AbstractOpenBISNode {

    private static final long serialVersionUID = 1L;

    private Space s;

    boolean tagsRetrieved = false;
    public List<Sample> tags;

    /**
     * Constructor
     * @param s SpaceWithProjectsAndRoleAssignments object
     */
    public OpenBISSpaceNode(Space s) {
        super(s);
        this.s = s;

        this.tags = new ArrayList<Sample>();
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
    @Override
    public javax.swing.Icon getIcon() {
        return new javax.swing.ImageIcon(
                getClass().getResource("icons/space.png"));
    }

    /**
     * Get the tooltip to be displayed when the user places the mouse
     * over the Node in the JTree
     * @return the tooltip to be displayed over the Node
     */
    @Override
    public String getTooltip() { return "openBIS space"; }

    /**
     * Space has children and is therefore not a leaf
     * @return false
     */
    @Override
    public boolean isLeaf() {
        return false;
    }

}
