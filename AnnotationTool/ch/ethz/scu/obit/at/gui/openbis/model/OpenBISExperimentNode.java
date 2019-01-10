package ch.ethz.scu.obit.at.gui.openbis.model;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;

/**
 * A Custom Experiment TreeNode
 * @author Aaron Ponti
 *
 */
public class OpenBISExperimentNode extends AbstractOpenBISNode {

    private static final long serialVersionUID = 1L;

    private Experiment e;

    /**
     * Constructor
     * @param e Experiment object
     */
    public OpenBISExperimentNode(Experiment e) {
        super(e);
        this.e = e;
    }

    /**
     * Returns the type of the node
     * @return Type of the node
     */
    @Override
    public String getType() { return "Experiment"; }

    /**
     * String representation of the node
     * @return String representation of the node
     */
    @Override
    public String toString() {
        return e.getCode();
    }

    /**
     * Experiment code
     * @return Experiment code
     */
    @Override
    public String getCode() { return e.getCode(); }

    /**
     * Experiment identifier
     * @return Experiment identifier
     */
    @Override
    public String getIdentifier() { return e.getIdentifier().toString(); }

    /**
     * Get the icon to be displayed in the JTree
     * @return the icon to be displayed in the JTree
     */
    @Override
    public javax.swing.Icon getIcon() {
        return new javax.swing.ImageIcon(
                getClass().getResource("icons/experiment.png"));
    }

    /**
     * Get the tooltip to be displayed when the user places the mouse
     * over the Node in the JTree
     * @return the tooltip to be displayed over the Node
     */
    @Override
    public String getTooltip() { return e.getCode(); }

    /**
     * Experiment has children and is therefore not a leaf
     * @return false
     */
    @Override
    public boolean isLeaf() {
        return false;
    }

}
