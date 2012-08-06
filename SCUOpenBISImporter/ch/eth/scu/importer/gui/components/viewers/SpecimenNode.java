package ch.eth.scu.importer.gui.components.viewers;

import ch.eth.scu.importer.gui.descriptors.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class SpecimenNode extends CustomTreeNode {

	private static final long serialVersionUID = 1L;

	public SpecimenNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(getClass().getResource("specimen.png"));
	}

	public String getTooltip() {
		return "Specimen";
	}
}