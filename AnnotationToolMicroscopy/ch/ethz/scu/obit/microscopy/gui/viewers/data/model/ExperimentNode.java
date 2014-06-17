package ch.ethz.scu.obit.microscopy.gui.viewers.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class ExperimentNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public ExperimentNode(ExperimentDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/experiment.png"));
	}

	public String getTooltip() {
		return "Experiment";
	}
}