package ch.ethz.scu.obit.bdfacsdivafcs.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.model.SampleDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class TubeNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param object A sample descriptor. 
	 */
	public TubeNode(SampleDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/tube.png"));
	}
	
	public String getTooltip() {
		return "Tube";
	}
}