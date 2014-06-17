package ch.ethz.scu.obit.at.gui.viewers.data.model;

import ch.ethz.scu.obit.processors.data.model.RootDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for
 * different Node types
 * @author Aaron Ponti 
 */
public class RootNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public RootNode(RootDescriptor object) {
		super(object);
		this.type = "root";
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/root.png"));
	}
	
	@Override
	public String toString() {
		return "/" + this.getUserObject().toString();
	}
}