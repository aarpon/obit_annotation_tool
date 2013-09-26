package ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.lsrfortessa.processors.model.SampleDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class SpecimenNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

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