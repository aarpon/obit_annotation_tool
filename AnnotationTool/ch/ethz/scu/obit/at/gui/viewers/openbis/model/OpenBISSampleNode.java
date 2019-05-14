package ch.ethz.scu.obit.at.gui.viewers.openbis.model;


import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;


/**
 * A Custom Sample TreeNode
 * @author Aaron Ponti
 *
 */
public class OpenBISSampleNode extends AbstractOpenBISNode {

    private static final long serialVersionUID = 1L;

    private Sample s;

    /**
     * Constructor
     * @param s Sample object
     */
    public OpenBISSampleNode(Sample s) {
        super(s);
        this.s = s;
    }

    /**
     * Returns the type of the node
     * @return Type of the node
     */
    @Override
    public String getType() { return "Experiment sample"; }

    /**
     * String representation of the node
     * @return String representation of the node
     */
    @Override
    public String toString() {
        // Get the name if present or fall back to the sample code
        Map<String, String> properties = s.getProperties();
        if (properties.isEmpty()) {
            return s.getCode();
        }
        if (properties.containsKey("$NAME")) {
            return properties.get("$NAME");
        } else {
            return s.getCode();
        }
    }

    /**
     * Sample code
     * @return Sample code
     */
    @Override
    public String getCode() { return s.getCode(); }

    /**
     * Sample identifier
     * @return Sample identifier
     */
    @Override
    public String getIdentifier() { return s.getIdentifier().getIdentifier(); }

    /**
     * Get the icon to be displayed in the JTree
     * @return the icon to be displayed in the JTree
     */
    @Override
    public javax.swing.Icon getIcon() {
        return new javax.swing.ImageIcon(
                getClass().getResource("icons/experiment_sample.png"));
    }

    /**
     * Get the tooltip to be displayed when the user places the mouse
     * over the Node in the JTree
     * @return the tooltip to be displayed over the Node
     */
    @Override
    public String getTooltip() { return "Experiment sample"; }

    /**
     * Sample does not have children and is therefore a leaf
     * @return true
     */
    @Override
    public boolean isLeaf() {
        return true;
    }

}
