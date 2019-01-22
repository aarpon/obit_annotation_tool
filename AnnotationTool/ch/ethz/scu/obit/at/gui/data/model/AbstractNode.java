package ch.ethz.scu.obit.at.gui.data.model;

import javax.swing.tree.DefaultMutableTreeNode;

import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree that renders the hierarchical
 * experiment content of datasets
 * @author Aaron Ponti
 */
public abstract class AbstractNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    protected String type;

    /**
     * Constructor.
     * @param object A descriptor.
     */
    public AbstractNode(AbstractDescriptor object) {
        super(object);
        this.type = object.getType();
    }

    /**
     * Return the icon to be rendered in the tree viewer.
     * @return icon to be renderd.
     */
    public abstract javax.swing.Icon getIcon();

    /**
     * Return the type of the node.
     * @return type of the node.
     */
    public String getType() { return type; }

    /**
     * Return the tooltip to be displayed on the node on mouseover.
     * @return tooltip string.
     */
    public String getTooltip() { return ""; }

}