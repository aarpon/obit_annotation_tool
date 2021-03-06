package ch.ethz.scu.obit.at.gui.viewers.openbis.model;


import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;

/**
 * A Custom Project TreeNode
 * @author Aaron Ponti
 *
 */
public class OpenBISProjectNode extends AbstractOpenBISNode {

    private static final long serialVersionUID = 1L;

    private Project p;

    /**
     * Constructor
     * @param p Project object
     */
    public OpenBISProjectNode(Project p) {
        super(p);
        this.p = p;
    }

    /**
     * Returns the type of the node
     * @return Type of the node
     */
    @Override
    public String getType() { return "Project"; }

    /**
     * String representation of the node
     * @return String representation of the node
     */
    @Override
    public String toString() { return p.getCode(); }

    /**
     * Project code
     * @return Project code
     */
    @Override
    public String getCode() { return p.getCode(); }

    /**
     * Project identifier
     * @return Project identifier
     */
    @Override
    public String getIdentifier() { return p.getIdentifier().toString(); }

    /**
     * Get the icon to be displayed in the JTree
     * @return the icon to be displayed in the JTree
     */
    @Override
    public javax.swing.Icon getIcon() {
        return new javax.swing.ImageIcon(
                getClass().getResource("icons/project.png"));
    }

    /**
     * Get the tooltip to be displayed when the user places the mouse
     * over the Node in the JTree
     * @return the tooltip to be displayed over the Node
     */
    @Override
    public String getTooltip() { return "openBIS project"; }

    /**
     * Project has children but we currently do not allow to expand.
     * @return false
     */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /**
     * Get the identifier of the containing space.
     * @return The space identifier.
     * @return
     */
    public String getSpaceIdentifier() {
        return identifier;
    }

}
