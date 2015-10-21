package ch.ethz.scu.obit.at.gui.viewers.data.model;

import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class ExperimentNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param object An Experiment descriptor. 
	 */
	public ExperimentNode(ExperimentDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	/**
	 * Return the icon for the experiment.
	 * @return icon for the experiment.
	 */
	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/experiment.png"));
	}

	/**
	 * Return the tooltip for the Experiment node.
	 * @return Tooltip for the Experiment node. 
	 */
	public String getTooltip() {
		return "Experiment";
	}
}