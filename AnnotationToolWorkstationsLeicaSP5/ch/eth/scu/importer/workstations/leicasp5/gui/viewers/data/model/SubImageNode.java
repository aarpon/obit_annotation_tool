package ch.eth.scu.importer.workstations.leicasp5.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class SubImageNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public SubImageNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("./icons/subimage.png"));
	}
	
	public String getTooltip() {
		return "Sub-image";
	}
}