package ch.ethz.scu.obit.at.gui.data.model;

import ch.ethz.scu.obit.processors.data.model.RootDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for
 * different Node types
 * @author Aaron Ponti 
 */
public class RootNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param object A Root object.
	 */
	public RootNode(RootDescriptor object) {
		super(object);
		this.type = "root";
	}

	/**
	 * Return the icon for the root node.
	 * @return icon for the root node.
	 */
	@Override
	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/root.png"));
	}
	
	/**
	 * Node name to be displayed.
	 * Node name (underlying descriptor name).
	 */
	@Override
	public String toString() {
		return "/" + this.getUserObject().toString();
	}
	
	/**
	 * Return tooltip for the root node.
	 * @return tooltip for the root node.
	 */
	@Override
	public String getTooltip() { return "The root of your user folder."; }
}