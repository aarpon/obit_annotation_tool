package ch.ethz.scu.obit.flow.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.processors.data.model.SampleDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class SpecimenNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param object A sample descriptor. 
	 */
	public SpecimenNode(SampleDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/specimen.png"));
	}

	public String getTooltip() {
		return "Specimen";
	}
}