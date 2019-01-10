package ch.ethz.scu.obit.at.gui.data.model;

import ch.ethz.scu.obit.processors.data.model.CollectionDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti
 */
public class CollectionNode extends AbstractNode {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * @param object An Experiment descriptor.
     */
    public CollectionNode(CollectionDescriptor object) {
        super(object);
        this.type = object.getType();
    }

    /**
     * Return the icon for the experiment.
     * @return icon for the experiment.
     */
    @Override
    public javax.swing.Icon getIcon() {
        return new javax.swing.ImageIcon(
                getClass().getResource("icons/collection.png"));
    }

    /**
     * Return the tooltip for the Collection node.
     * @return Tooltip for the Collection node.
     */
    @Override
    public String getTooltip() {
        return "Experiment collection";
    }
}