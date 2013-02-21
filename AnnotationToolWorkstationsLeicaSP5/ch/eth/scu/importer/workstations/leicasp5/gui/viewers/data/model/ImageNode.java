package ch.eth.scu.importer.workstations.leicasp5.gui.viewers.data.model;

import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.model.AbstractDescriptor;

/**
 * Customized Node to be used in a JTree allowing different icons for different
 * Node types
 * @author Aaron Ponti 
 */
public class ImageNode extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public ImageNode(AbstractDescriptor object) {
		super(object);
		this.type = object.getType();
	}

	public javax.swing.Icon getIcon() {
		return new javax.swing.ImageIcon(
				getClass().getResource("icons/image.png"));
	}
	
	public String getTooltip() {
		return "Image";
	}
}